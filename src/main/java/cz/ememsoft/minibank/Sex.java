package cz.ememsoft.minibank;

public enum Sex {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String sex;

    Sex(final String sex) {
        this.sex = sex;
    }
}
