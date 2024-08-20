package Backend.BangWool.cosmetics.domain;

public enum Category {
    basic, base, color, others;

    public static boolean contains(String value) {
        for (Category category : Category.values()) {
            if (category.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

}
