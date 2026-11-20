# Reka Bentuk: JARVIS + Tab Taklimat (Fasa 1)

Tarikh: 2026-11-20 · Status: diluluskan pengguna

## Tujuan
Setiap pagi JARVIS beri satu taklimat bersuara yang lengkap supaya pengguna tak perlu buka banyak app. Diilhamkan oleh preset "morning-digest" OpenJarvis, tapi dibina sebagai app Android sendiri.

## Skop
App JARVIS dibina baru dalam repo `mohdrozaimi205/JARVIS` (repo lama kosong). Fasa 1 = kerangka app JARVIS + Tab Taklimat. Ciri JARVIS lain (kawal TV, waktu solat, wake word "Hey JARVIS") = pelan berasingan kemudian.

## Matlamat (Fasa 1)
1. Taklimat dicetuskan pada masa kejut pagi yang ditetapkan, atau bila pengguna sebut "good morning" / tekan butang.
2. Sapaan English British, isi Bahasa Melayu, satu suara.
3. Kandungan ikut urutan: tarikh + cuaca (MET Malaysia, data.gov.my); IPU anggaran (Open-Meteo, dilabel "anggaran"); mesyuarat hari ini dari kalendar telefon (Google + Outlook jika sync dibenarkan); kesihatan (Health Connect) + cadangan pemakanan (Gemini); berita utama Malaysia & dunia (3–5 tajuk, RSS).
4. Atas permintaan: "jarak dari A ke B" → KM + anggaran masa (OpenRouteService).
5. Paparan: satu skrin tanpa scroll, kad-kad; rekaan baru (bukan gaya projek lepas).

## Bukan matlamat (Fasa 2)
Email Gmail, Outlook (akaun syarikat bhpetrol.net.my), SharePoint, kadar tol A→B.

## Anti-matlamat
- Tiada kunci API dalam kod/repo awam — pengguna masukkan dalam Tetapan.
- Tiada data dihantar ke pelayan selain sumber yang disenaraikan; data kesihatan hanya ke Gemini untuk cadangan pemakanan.

## Had
- Semua sumber percuma. Kunci percuma diperlukan: Gemini, OpenRouteService.
- Suara lelaki BM bergantung pada enjin TTS dalam telefon (Google TTS BM lalai perempuan) — guna suara terbaik yang ada; boleh dipilih dalam Tetapan.
- Telefon utama: Samsung Galaxy A16 5G, Honor 200 5G; tablet Honor Pad X8.

## Pendekatan dipilih
A — tab baru dalam app JARVIS (bukan app berasingan / widget).

## Keputusan pembina (cadangan Claude)
- Kotlin + Jetpack Compose, minSdk 26.
- Lokasi cuaca MET: padankan nama daerah dari Geocoder dengan senarai lokasi MET; boleh tukar manual dalam Tetapan.
- IPU: US AQI Open-Meteo dipetakan ke kategori IPU (Baik / Sederhana / Tidak Sihat / Sangat Tidak Sihat / Berbahaya).
- APK dibina oleh GitHub Actions → Releases; juga dimuat naik ke Google Drive "Apk Android Studio".
