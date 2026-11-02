# SteamLite

[Polski](#polski) | [English](#english)

## Polski

SteamLite to mój projekt desktopowej aplikacji inspirowanej platformami
dystrybucji gier. Napisałem go w **Javie 17**, z interfejsem w **Swingu**.
Aplikacja łączy sklep, bibliotekę gier, funkcje społecznościowe i obsługę
zgłoszeń. Model domenowy obejmuje konta klientów, twórców i pracowników wsparcia.

### Funkcje

- **Konta użytkowników** — rejestracja, logowanie i edycja danych profilu.
- **Sklep i biblioteka** — przeglądanie gier, symulowane zakupy z salda portfela,
  biblioteka użytkownika i historia transakcji.
- **Promocje** — rabaty procentowe i kwotowe oraz kody promocyjne.
- **Społeczność** — recenzje, oceny ich przydatności, opisy modyfikacji
  oraz osiągnięcia związane z grami i aktywnością na platformie.
- **Panel twórcy** — tworzenie i publikowanie pozycji w katalogu gier.
- **Wsparcie techniczne** — zgłoszenia klientów, wymiana wiadomości,
  przejmowanie spraw do obsługi i zamykanie zgłoszeń.
- **Zapis lokalny** — przechowywanie stanu aplikacji między uruchomieniami.

### Technologie

| Obszar | Technologie |
| --- | --- |
| Język | Java 17 |
| Interfejs | Swing, AWT |
| Trwałość danych | Serializacja obiektów, `ObjectPlus` |
| Budowanie i testy | `javac`, PowerShell, testy regresyjne w Javie |

Do kompilacji wystarczy JDK; projekt korzysta z bibliotek standardowych Javy.
Interfejs aplikacji jest w języku angielskim.

### Uruchomienie

Wymagania: **JDK 17 lub nowsze**, Windows i PowerShell. Ustaw `JAVA_HOME`
albo dodaj katalog `bin` JDK do zmiennej `PATH`.

W katalogu projektu uruchom:

```powershell
.\run.cmd
```

Możesz też otworzyć `run.cmd` dwuklikiem. Skrypt kompiluje źródła z `src/`,
aktualizuje pliki w `out/` i uruchamia `app.gui.GuiMain`. Ścieżki są ustalane
względem katalogu projektu, więc skrypt działa także po przeniesieniu projektu
lub wywołaniu go z innego katalogu roboczego.

### Pierwsze logowanie

Konto można utworzyć w zakładce **Register**. Gdy plik danych jeszcze nie istnieje,
aplikacja tworzy przykładowy katalog gier i konta zdefiniowane w `SampleData`:

| Rola | Login | Hasło | Opcja Account type |
| --- | --- | --- | --- |
| Klient | `guiUser1` | `pass12345` | Customer |
| Twórca | `guiUser3` | `pass12345` | Customer |
| Wsparcie techniczne | `support1` | `pass12345` | Tech Support |

Są to konta demonstracyjne dla nowo utworzonego pliku danych. Przy kolejnych
uruchomieniach aplikacja wczytuje istniejące konta i zapisany stan.

### Kompilacja i testy

```powershell
.\run.cmd -BuildOnly
powershell -NoProfile -ExecutionPolicy Bypass -File .\test.ps1
```

Pierwsze polecenie kompiluje aplikację. Drugie kompiluje kod i uruchamia
7 scenariuszy regresyjnych: wczytanie istniejących danych, pierwszy start,
zapis i ponowny odczyt, własną ścieżkę danych, uszkodzony plik, błąd odczytu
oraz nieudany zapis. Testy sprawdzają również uruchamianie po przeniesieniu
projektu, ścieżki ze spacjami i zachowanie danych przy błędach.

Scenariusze działają na kopiach danych w katalogach tymczasowych oraz w trybie
bez interfejsu graficznego. Wyniki są wypisywane w terminalu.

### Dane aplikacji

Domyślny zapis znajduje się w `data/ObjectPlus.data` w katalogu projektu.
Przy pierwszym uruchomieniu brakujący katalog i plik są tworzone automatycznie.
Błąd odczytu istniejącego zapisu jest zgłaszany, a plik pozostaje zachowany.
Zapis korzysta z pliku tymczasowego i zastąpienia pliku docelowego po zakończeniu
serializacji.

Po kompilacji można wskazać inny plik danych przy bezpośrednim uruchomieniu Javy:

```powershell
java '-Dsteamlite.dataFile=C:\SteamLiteData\ObjectPlus.data' -cp out app.gui.GuiMain
```

Powyższe polecenie uruchom z katalogu projektu. Klasa `app.main` zawiera osobny
scenariusz demonstracyjny modelu i zapisuje własne dane przykładowe. Głównym
punktem wejścia interfejsu jest `app.gui.GuiMain`.

### Struktura projektu

```text
src/
  app/gui/       okna Swing, sesja użytkownika i stan aplikacji
  app/main.java  demonstracja modelu domenowego
  core/          gry, konta, zakupy, recenzje i mechanizm ObjectPlus
  achievements/  osiągnięcia platformowe
  community/     profile i role społecznościowe
  promotion/     promocje procentowe i kwotowe
  ticket/        zgłoszenia i ich stany
tests/           testy regresyjne uruchamiania i zapisu danych
data/            lokalny zapis aplikacji
```

Model wykorzystuje dziedziczenie, interfejsy, kompozycję i dwukierunkowe
asocjacje. `ObjectPlus` odpowiada za ekstensje klas i ich trwałość.
Katalogi `out/` i `.build/` zawierają pliki generowane i są pomijane przez Git.

W IDE ustaw JDK 17+, oznacz `src/` jako katalog źródłowy i uruchom klasę
`app.gui.GuiMain`.

---

## English

SteamLite is my desktop application inspired by game distribution platforms.
I built it with **Java 17** and a **Swing** interface. It combines a game store,
a personal library, community features and support ticket handling. The domain
model covers customer, creator and support staff accounts.

### Features

- **User accounts** — registration, login and profile editing.
- **Store and library** — browse games, make simulated purchases using a wallet
  balance, manage a personal library and view purchase history.
- **Promotions** — percentage and fixed-amount discounts, plus promotional codes.
- **Community** — reviews, helpfulness votes, mod descriptions and achievements
  related to games and platform activity.
- **Creator panel** — create and publish entries in the game catalog.
- **Technical support** — customer tickets, message threads, taking ownership
  of cases and closing tickets.
- **Local persistence** — retain application state between sessions.

### Technologies

| Area | Technologies |
| --- | --- |
| Language | Java 17 |
| Interface | Swing, AWT |
| Persistence | Object serialization, `ObjectPlus` |
| Build and testing | `javac`, PowerShell, Java regression tests |

A JDK is sufficient to compile the project; it uses Java's standard libraries.
The application interface is in English.

### Getting started

Requirements: **JDK 17 or newer**, Windows and PowerShell. Set `JAVA_HOME`
or add your JDK's `bin` directory to `PATH`.

Run the following command from the project directory:

```powershell
.\run.cmd
```

You can also double-click `run.cmd`. The script compiles sources from `src/`,
updates the files in `out/` and launches `app.gui.GuiMain`. Paths are resolved
relative to the project directory, so the script also works after moving the
project or calling it from a different working directory.

### First login

Create an account in the **Register** tab. When the data file does not yet exist,
the application creates a sample game catalog and accounts defined in `SampleData`:

| Role | Username | Password | Account type option |
| --- | --- | --- | --- |
| Customer | `guiUser1` | `pass12345` | Customer |
| Creator | `guiUser3` | `pass12345` | Customer |
| Technical support | `support1` | `pass12345` | Tech Support |

These are demonstration accounts for a newly created data file. On subsequent
runs, the application loads the existing accounts and saved state.

### Building and testing

```powershell
.\run.cmd -BuildOnly
powershell -NoProfile -ExecutionPolicy Bypass -File .\test.ps1
```

The first command compiles the application. The second compiles the code and
runs 7 regression scenarios: loading existing data, first startup, saving and
reloading, a custom data path, a corrupt file, a read error and a failed save.
Tests also cover running the project after relocation, paths containing spaces
and preserving data when errors occur.

Scenarios use copies of the data in temporary directories and run without a
graphical interface. Results are printed in the terminal.

### Application data

The default save file is `data/ObjectPlus.data` inside the project directory.
The directory and file are created automatically on the first run if missing.
Errors reading an existing save are reported while preserving the file.
Saving uses a temporary file and replaces the target once serialization completes.

After compilation, you can select a different data file by launching Java directly:

```powershell
java '-Dsteamlite.dataFile=C:\SteamLiteData\ObjectPlus.data' -cp out app.gui.GuiMain
```

Run this command from the project directory. The `app.main` class contains a
separate domain model demonstration and saves its own sample data.
The main entry point for the interface is `app.gui.GuiMain`.

### Project structure

```text
src/
  app/gui/       Swing windows, user sessions and application state
  app/main.java  domain model demonstration
  core/          games, accounts, purchases, reviews and ObjectPlus
  achievements/  platform achievements
  community/     community profiles and roles
  promotion/     percentage and fixed-amount promotions
  ticket/        tickets and their states
tests/           startup and persistence regression tests
data/            local application save
```

The model uses inheritance, interfaces, composition and bidirectional
associations. `ObjectPlus` manages class extents and their persistence.
The `out/` and `.build/` directories contain generated files and are ignored by Git.

In your IDE, select JDK 17+, mark `src/` as a source directory and run
`app.gui.GuiMain`.
