#!/usr/bin/env python3
#
# SPDX-FileCopyrightText: 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#
"""Put the classes.dex of a d8 output zip in front of an APK's dex files.

ART resolves a class from the first dex of the APK that defines it, so the
compat classes shadow the stock ones with the same name. Everything else is
copied unchanged, except the retired Lenovo IME service in the binary manifest
when --remove-pen-ime is given (only the APK that carries the duplicate IME
service needs that; the other callers keep their manifest intact).
The APK is re-signed and aligned by android_app_import.
"""
import argparse
import re
import struct
import zipfile


PEN_IME_CLASS = "com.lenovo.pen.handwriting.service.HandwritingIme"
ANDROID_NS = "http://schemas.android.com/apk/res/android"


def _strings(chunk):
    count, _, flags, start, _ = struct.unpack_from("<IIIII", chunk, 8)
    header_size = struct.unpack_from("<H", chunk, 2)[0]
    utf8 = bool(flags & 0x100)

    def length(offset, width):
        fmt, high = ("<B", 0x80) if width == 1 else ("<H", 0x8000)
        value = struct.unpack_from(fmt, chunk, offset)[0]
        offset += width
        if value & high:
            value = ((value & (high - 1)) << (width * 8)) | struct.unpack_from(
                    fmt, chunk, offset)[0]
            offset += width
        return value, offset

    result = []
    for i in range(count):
        offset = start + struct.unpack_from("<I", chunk, header_size + 4 * i)[0]
        size, offset = length(offset, 1 if utf8 else 2)
        if utf8:
            size, offset = length(offset, 1)  # first length is UTF-16 code units
        else:
            size *= 2
        result.append(chunk[offset:offset + size].decode("utf-8" if utf8 else "utf-16-le"))
    return result


def remove_pen_ime(manifest):
    """Remove exactly the legacy IME service, retaining resources and other components."""
    xml_type, header_size, total = struct.unpack_from("<HHI", manifest)
    if xml_type != 3 or total != len(manifest):
        raise ValueError("Expected a complete binary Android manifest")
    chunks = []
    strings = None
    skip_depth = 0
    removed = 0
    offset = header_size
    while offset < total:
        kind, node_header, size = struct.unpack_from("<HHI", manifest, offset)
        if size < node_header or size < 8 or offset + size > total:
            raise ValueError("Invalid binary XML chunk")
        chunk = manifest[offset:offset + size]
        offset += size
        if kind == 1:
            strings = _strings(chunk)
        if skip_depth:
            if kind == 0x102:
                skip_depth += 1
            elif kind == 0x103:
                skip_depth -= 1
            continue
        if kind == 0x102:  # RES_XML_START_ELEMENT_TYPE
            if strings is None:
                raise ValueError("Missing manifest string pool")
            _, name, attr_start, attr_size, attr_count = struct.unpack_from(
                    "<IIHHH", chunk, node_header)
            if strings[name] == "service":
                for i in range(attr_count):
                    base = node_header + attr_start + i * attr_size
                    ns, key, raw = struct.unpack_from("<III", chunk, base)
                    if ns != 0xffffffff and strings[ns] == ANDROID_NS and strings[key] == "name":
                        if raw == 0xffffffff:
                            if chunk[base + 15] != 3:
                                raise ValueError("Expected string service name")
                            raw = struct.unpack_from("<I", chunk, base + 16)[0]
                        if strings[raw] == PEN_IME_CLASS:
                            skip_depth = 1
                            removed += 1
                            break
                if skip_depth:
                    continue
        chunks.append(chunk)
    if removed != 1 or skip_depth:
        raise ValueError(f"Expected exactly one complete Lenovo IME service, found {removed}")
    result = bytearray(manifest[:header_size] + b"".join(chunks))
    struct.pack_into("<I", result, 4, len(result))
    return bytes(result)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apk", required=True)
    ap.add_argument("--dex-zip", required=True)
    ap.add_argument("--out", required=True)
    ap.add_argument("--remove-pen-ime", action="store_true",
                    help="strip the retired Lenovo handwriting IME service")
    args = ap.parse_args()

    with zipfile.ZipFile(args.dex_zip) as dz:
        compat = dz.read("classes.dex")

    dex_re = re.compile(r"^classes(\d*)\.dex$")
    with zipfile.ZipFile(args.apk) as src, \
            zipfile.ZipFile(args.out, "w") as out:
        out.writestr(zipfile.ZipInfo("classes.dex", date_time=(2008, 1, 1, 0, 0, 0)), compat,
                     compress_type=zipfile.ZIP_DEFLATED)
        for info in src.infolist():
            if info.filename.startswith("META-INF/"):
                continue  # old signature
            data = src.read(info.filename)
            if info.filename == "AndroidManifest.xml" and args.remove_pen_ime:
                data = remove_pen_ime(data)
            m = dex_re.match(info.filename)
            name = info.filename
            if m:
                n = int(m.group(1) or 1)
                name = f"classes{n + 1}.dex"
            zi = zipfile.ZipInfo(name, date_time=info.date_time)
            zi.external_attr = info.external_attr
            out.writestr(zi, data, compress_type=info.compress_type)


if __name__ == "__main__":
    main()
