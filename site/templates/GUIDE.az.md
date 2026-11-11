# Zovrax — yeni mod əlavə etmə təlimatı

Bu `mods/_template/` qovluğu **hazır şablondur, real mod deyil**. Alt xəttlə (`_`) başlayan qovluqlar saytın kataloquna avtomatik daxil edilmir.

## 1. Mod qovluğunu yarat

Zovrax repositorisini **Fork** et, öz hesabında branch yarat və şablonu yeni modun qovluğuna kopyala:

```bash
git checkout -b add-my-mod
cp -R mods/_template mods/my-mod
```

`my-mod` yerinə moduna uyğun kiçik hərf və tire ilə unikal ad yaz. Məsələn, `better-inventory`. Sonra `zovrax.json` içində `slug` mütləq həmin qovluq adı ilə eyni olsun.

## 2. Hansı fayl nə üçündür?

- **`zovrax.json`** — modun adı, kateqoriyası, statusu, logosu, müəllifləri, uyğun Minecraft versiyaları, Fabric/NeoForge loader-ləri, buraxılışlar və `.jar` yolları.
- **`ZOVRAX.md`** — saytın **Overview** bölməsində görünəcək əsas Markdown təsviri (ingilis dili).
- **`ZOVRAX.ru.md`** və **`ZOVRAX.fr.md`** — rus və fransız tərcümələri (istəyə bağlıdır).
- **`README.md`** — GitHub-da proqramçılar üçün modun qaynaq kodu, quraşdırılması və build təlimatı; saytın əsas təsviri **deyil**.
- **`assets/icon.svg`** — demo ikon. Öz logonu yerləşdir və adını dəyişirsənsə JSON-dakı `icon` yolunu yenilə.
- **`assets/preview.png`** — istəyə bağlı ekran şəkli. Real faylı əlavə etməmisənsə, JSON-da göstərmə.
- **`releases/`** — yalnız həqiqi, yoxlanmış `.jar` mod faylları yerləşdirilir.
- **`src/`** — həqiqi Java mənbə kodu; seçdiyin loader-in Gradle konfiqurasiyasını da əlavə et.

## 3. JSON-un əsas sahələri

| Sahə | Nə yazılmalıdır |
| --- | --- |
| `schemaVersion` | `2` |
| `slug` | Qovluqla tam eyni unikal ad |
| `name` | Saytda görünən mod adı |
| `category` | `hud`, `quality`, `gameplay`, `visual`, `utility`, `performance` |
| `status` | `planned`, `development`, `released` və ya `archived` |
| `icon` | `assets/icon.svg`, yaxud logo yoxdursa `null` |
| `summary` | `en` mütləq, `ru` və `fr` istəyə bağlı |
| `minecraftVersions` | Məsələn `["1.21.10"]`; yalnız həqiqi uyğunluq |
| `loaders` | Məsələn `["Fabric"]` |
| `authors` | `site/data/catalog.json` daxilindəki `developers[].id` dəyərləri |
| `screenshots` | `src` və `alt` sahəli şəkil obyektləri; real fayl olmalıdır |
| `releases` | Hələ hazır deyilsə `[]`, real build olduqda versiyalara görə fayllar |

İlkin şablon `planned` statusundadır, `releases: []` saxlanılıb. Bu, **yükləmə yoxdur** deməkdir.

## 4. Versiyalar və `.jar` faylları

Hər buraxılışın `version` sahəsi mod versiyasıdır (məsələn `1.0.0`). `minecraftVersions` oyunun versiyasıdır (məsələn `1.21.10`). Bunları qarışdırma.

`releases/release-entry.example.json` faylı bir neçə mod versiyasını və Fabric/NeoForge üçün ayrıca JAR yollarını göstərir. O yalnız nümunədir və sayt tərəfindən avtomatik oxunmur. Real, test edilmiş `.jar` yerləşdirəndən sonra nümunədəki `releases` massivini **`zovrax.json`** daxilinə köçür, fayl yollarını öz fayllarına uyğunlaşdır və `status`-u `released` et.

Fayl yolu modun öz qovluğuna nəzərən yazılır: `releases/my-mod-1.0.0-fabric.jar`. Burada `../` işlətmə. Sayt buraxılış, Minecraft versiyası və loader üzrə filtrləyib real JAR-ı bir kliklə yükləyəcək.

## 5. Test və göndərmə

Repo kökündən bunları icra et:

```bash
node .github/scripts/build-catalog.mjs
node .github/scripts/validate-catalog.mjs
python -m http.server 8000 --directory site
```

Brauzerdə `http://localhost:8000/#/mods/my-mod` aç və modunu yoxla. Şablon `mods/_template/` adı ilə saytda çıxmamalıdır.

Yeni müəllifdirsənsə, `site/data/catalog.json` faylında `developers` siyahısına öz müəllif ID-ni və düzgün profil məlumatlarını əlavə et. Sonra Fork-un üzərindəki branch-dan əsas Zovrax repositorisinə **Pull Request** aç. Avtomatik yoxlama və sahibin təsdiqindən sonra `main`-ə merge edilir, sonra GitHub Pages saytı yeniləyir.

**Diqqət:** Saxta `.jar`, uydurma uyğunluq, başqasına məxsus şəkil, gizli açar və ya başqasının ianə linkini əlavə etmə. Daha geniş ingilisdilli izah: [START-HERE.md](START-HERE.md).
