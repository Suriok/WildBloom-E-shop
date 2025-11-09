package dao;

import model.Administrator;
import org.springframework.stereotype.Repository;

@Repository
public class AdministratorDao extends BaseDao<Administrator> {
    public AdministratorDao() { super(Administrator.class); }
}

