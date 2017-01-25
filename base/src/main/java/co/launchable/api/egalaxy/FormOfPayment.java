package co.launchable.api.egalaxy;

/**
 * Created with IntelliJ IDEA.
 * User: michaelmcelligott
 * Date: 8/26/13
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class FormOfPayment {
    private int id;
    private String code;
    private String name;

    public FormOfPayment(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
