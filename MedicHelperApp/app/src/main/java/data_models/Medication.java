package data_models;

public class Medication {

    private int medicationId;
    private String name;
    private int inventory;
    private String image;

    public Medication(int medicationId, String name, int inventory, String image) {
        this.medicationId = medicationId;
        this.name = name;
        this.inventory = inventory;
        this.image = image;
    }

    public Medication(int medicationId, String name, int inventory) {
        this.medicationId = medicationId;
        this.name = name;
        this.inventory = inventory;
    }

    public Medication(String name, int inventory) {
        this.name = name;
        this.inventory = inventory;
    }

    public Medication(String name, int inventory, String image) {
        this.name = name;
        this.inventory = inventory;
        this.image=image;
    }

    public int getMedicationId() {
        return medicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Medication{" +
                "id=" + medicationId +
                "name='" + name + '\'' +
                ", inventory=" + inventory +
                ", imageBase64='" + image + '\'' +
                '}';
    }
}
