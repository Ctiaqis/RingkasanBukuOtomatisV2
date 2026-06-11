# RingkasanBukuOtomatis-java

Nama repository: `RingkasanBukuOtomatis-java`

Package utama: `com.ringkasanbuku`

Repository ini dibuat **dari awal** berdasarkan:
1. PDF spesifikasi proyek aplikasi ringkasan buku otomatis.
2. Diagram class yang menampilkan kelas: `MainFrame`, `MainApp`, `Summarizer`, `RuleBasedSummarizer`, `ApiBasedSummarizer`, `TextInputHandler`, `SummarizerFactory`, `SummaryFormatter`, `HistoryRecord`, `SummaryHistoryManager`, `ConnectivityChecker`, dan `TokenValidator`.

## Fitur
- Input teks manual.
- Load teks dari file `.txt` dan `.pdf`.
- Load teks dari clipboard.
- Ringkasan otomatis dengan dua metode:
  - Rule-based
  - API-based (dengan fallback ke rule-based bila internet/token tidak valid)
- Simpan ringkasan ke `.txt`.
- Simpan ringkasan ke `.pdf`.
- Riwayat ringkasan disimpan di `history.json`.
- GUI desktop menggunakan Java Swing.

## Struktur Proyek
```text
src/main/java/com/ringkasanbuku/
├── app/
│   └── MainApp.java
├── core/
│   ├── Summarizer.java
│   ├── RuleBasedSummarizer.java
│   └── ApiBasedSummarizer.java
├── data/
│   ├── HistoryRecord.java
│   └── SummaryHistoryManager.java
├── gui/
│   └── MainFrame.java
├── support/
│   ├── TextInputHandler.java
│   ├── SummarizerFactory.java
│   └── SummaryFormatter.java
└── util/
    ├── ConnectivityChecker.java
    └── TokenValidator.java
```

## Cara Menjalankan
### Dengan Maven
```bash
mvn clean compile
mvn exec:java
```

### Build JAR
```bash
mvn clean package
```

## Cara Upload ke GitHub
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/USERNAME/RingkasanBukuOtomatis-java.git
git push -u origin main
```

Ganti `USERNAME` dengan username GitHub kamu.
