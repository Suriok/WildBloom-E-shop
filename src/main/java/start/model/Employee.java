package start.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "employee")
public class Employee extends User {

    private String position;

    @Temporal(TemporalType.DATE)
    private Date employmentStartDate;

    public Employee() {}

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Date getEmploymentStartDate() { return employmentStartDate; }
    public void setEmploymentStartDate(Date employmentStartDate) { this.employmentStartDate = employmentStartDate; }
}
