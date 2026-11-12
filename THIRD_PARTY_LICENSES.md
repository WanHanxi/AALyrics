# Third-Party Licenses

**Last reviewed:** November 7, 2026

AALyrics uses third-party open-source software. AALyrics itself is licensed separately under the repository LICENSE and NOTICE.

This file is intentionally limited to third-party software licensing and required attribution. External lyrics services, privacy terms, product-specific service terms, and brand assets are documented elsewhere where relevant.

## Apache License 2.0

The following software used by AALyrics is licensed under the Apache License 2.0:

- AndroidX / Jetpack, including Jetpack Compose, Activity, Lifecycle, Android for Cars App Library, Media, and Browser components
- Kotlin
- kotlinx.coroutines
- OkHttp
- Okio
- Gson
- Multiplatform Markdown Renderer
- JetBrains Markdown

Copyright remains with the respective upstream authors and contributors.

Apache License 2.0:

<https://www.apache.org/licenses/LICENSE-2.0>

Upstream projects:

- AndroidX: <https://github.com/androidx/androidx>
- Kotlin: <https://github.com/JetBrains/kotlin>
- kotlinx.coroutines: <https://github.com/Kotlin/kotlinx.coroutines>
- OkHttp: <https://github.com/square/okhttp>
- Okio: <https://github.com/square/okio>
- Gson: <https://github.com/google/gson>
- Multiplatform Markdown Renderer: <https://github.com/mikepenz/multiplatform-markdown-renderer>
- JetBrains Markdown: <https://github.com/JetBrains/markdown>

## MIT-licensed material in Multiplatform Markdown Renderer

The Multiplatform Markdown Renderer upstream project identifies portions derived from MarkdownComposer as MIT-licensed material.

Copyright © 2020 Erik Hellman

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Maintenance

When a release dependency is added, removed, or upgraded, this file should be checked for any new license or notice requirement.

Build-only and test-only dependencies are not listed unless they are distributed as part of the application.

## ICU-derived comparison data

The compact metadata comparison tables are generated with ICU 78.3 uconv,
from https://github.com/unicode-org/icu/tree/release-78.3. They cover BMP Han
Traditional-Simplified and Cyrillic-Latin single-character mappings. They are
comparison variants only; contextual conversions are not included. Regenerate
with `python3 scripts/generate-match-folds.py` and ICU 78.3. No ICU runtime is
bundled. The generated data is covered by the following Unicode license.

UNICODE LICENSE V3

COPYRIGHT AND PERMISSION NOTICE

Copyright © 2016-2025 Unicode, Inc.

NOTICE TO USER: Carefully read the following legal agreement. BY
DOWNLOADING, INSTALLING, COPYING OR OTHERWISE USING DATA FILES, AND/OR
SOFTWARE, YOU UNEQUIVOCALLY ACCEPT, AND AGREE TO BE BOUND BY, ALL OF THE
TERMS AND CONDITIONS OF THIS AGREEMENT. IF YOU DO NOT AGREE, DO NOT
DOWNLOAD, INSTALL, COPY, DISTRIBUTE OR USE THE DATA FILES OR SOFTWARE.

Permission is hereby granted, free of charge, to any person obtaining a
copy of data files and any associated documentation (the "Data Files") or
software and any associated documentation (the "Software") to deal in the
Data Files or Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, and/or sell
copies of the Data Files or Software, and to permit persons to whom the
Data Files or Software are furnished to do so, provided that either (a)
this copyright and permission notice appear with all copies of the Data
Files or Software, or (b) this copyright and permission notice appear in
associated Documentation.

THE DATA FILES AND SOFTWARE ARE PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF
THIRD PARTY RIGHTS.

IN NO EVENT SHALL THE COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE
BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL INDIRECT OR CONSEQUENTIAL DAMAGES,
OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THE DATA
FILES OR SOFTWARE.

Except as contained in this notice, the name of a copyright holder shall
not be used in advertising or otherwise to promote the sale, use or other
dealings in these Data Files or Software without prior written
authorization of the copyright holder.

SPDX-License-Identifier: Unicode-3.0
