package dao;

import model.Pracovnik;
import org.springframework.stereotype.Repository;

@Repository
public class PracovnikDao extends BaseDao<Pracovnik> {
    public PracovnikDao() { super(Pracovnik.class); }
}
