## Funkcjonalności

Aplikacja realizuje wszystkie historyjki użytkownika (User Stories) z zadań 1, 2 i 3:

* **Obsługa plików:** Wczytywanie oraz zapisywanie obrazów w formacie `.jpg`.
* **Manipulacja obrazem:**
    * Skalowanie do podanych wymiarów (z opcją przywrócenia oryginału).
    * Obrót o 90 stopni w lewo i prawo.
* **Wielowątkowe operacje graficzne:** (z ograniczeniem do maksymalnie 4 wątków jednocześnie)
    * Negatyw
    * Progowanie (z wyborem progu 0-255)
    * Konturowanie
* **Moduł Logowania:** Zapis historii wykonanych operacji, czasu ich wykonania oraz błędów do pliku tekstowego `app_log.txt`.

## Instrukcja uruchomienia

Aby uruchomić aplikację należy skorzystać z klasy **`Launcher.java`**
