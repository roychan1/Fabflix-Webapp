import java.util.ArrayList;

public class Star {
    private String name;
    private Integer birthYear;

    public void Star() {}
    public void Star(String name, Integer birthYear) {
        this.name = name;
        this.birthYear = birthYear;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }
    public Integer getBirthYear() {
        return birthYear;
    }

    public String toString() {
        return "name: " + name + ", birthYear: " + birthYear;
    }
}
