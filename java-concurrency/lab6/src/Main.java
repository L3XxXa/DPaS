import lab6_classes.Company;
import lab6_classes.Department;
import lab6_classes.Founder;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Company company = new Company(4);
        Founder founder = new Founder(company);
        founder.start();
    }

}
