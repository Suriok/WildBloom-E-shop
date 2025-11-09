package dao;

import model.Zakaznik;
import org.springframework.stereotype.Repository;

@Repository
public class ZakaznikDao extends BaseDao<Zakaznik> {
    public ZakaznikDao() { super(Zakaznik.class); }
}

