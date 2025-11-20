package model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "employee")
public class Employee extends User {

    private String position;

    @Temporal(TemporalType.DATE)
    private Date dateNastupu;

    public Employee() {}

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Date getdateNastupu() { return dateNastupu; }
    public void setdateNastupu(Date dateNastupu) { this.dateNastupu = dateNastupu; }
}
