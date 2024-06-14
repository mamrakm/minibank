package cz.ememsoft.minibank;

import lombok.Getter;

@Getter
public enum Nationality {
    AMERICAN("American"),
    ARGENTINIAN("Argentinian"),
    AUSTRALIAN("Australian"),
    BRAZILIAN("Brazilian"),
    BRITISH("British"),
    CANADIAN("Canadian"),
    CHINESE("Chinese"),
    FRENCH("French"),
    GERMAN("German"),
    INDIAN("Indian"),
    ISRAELI("Israeli"),
    ITALIAN("Italian"),
    JAPANESE("Japanese"),
    KOREAN("Korean"),
    MEXICAN("Mexican"),
    NEW_ZEALANDER("New Zealander"),
    RUSSIAN("Russian"),
    SOUTH_AFRICAN("South African"),
    SPANISH("Spanish");

    // Method to get the display name
    private final String displayName;

    // Constructor
    Nationality(final String displayName) {
        this.displayName = displayName;
    }

}
