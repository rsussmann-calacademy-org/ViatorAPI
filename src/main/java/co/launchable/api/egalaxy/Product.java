package co.launchable.api.egalaxy;

/**
 * Created with IntelliJ IDEA.
 * User: michaelmcelligott
 * Date: 5/28/13
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class Product {
    private String id;
    private String name;
    private String description;
    private Double amount;

    public Product(String id, String name, String description, Double amount) {
        this.id = trimNotNull(id);
        this.name = trimNotNull(name);
        this.description = trimNotNull(description);
        this.amount = amount;
    }

    private String trimNotNull(String in) {
        if (in != null)
            return in.trim();
        return in;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double aount) {
        this.amount = aount;
    }
}
