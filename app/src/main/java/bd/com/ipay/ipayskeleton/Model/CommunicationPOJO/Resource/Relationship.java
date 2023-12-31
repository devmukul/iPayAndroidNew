package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource;

public class Relationship implements Resource {
    private int id;
    private String name;

    public Relationship(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getStringId() {
        return null;
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

