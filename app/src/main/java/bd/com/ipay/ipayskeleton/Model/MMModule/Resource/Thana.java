package bd.com.ipay.ipayskeleton.Model.MMModule.Resource;

public class Thana {
    private int id;
    private String name;

    public Thana(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Thana{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
