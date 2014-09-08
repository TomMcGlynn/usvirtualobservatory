package org.nvo.eventtide.client.orm;

import org.nvo.eventtide.client.util.Compare;
import org.nvo.eventtide.client.util.Similar;

import java.io.Serializable;

public abstract class OrmBase implements Serializable, Similar {
    public abstract Long getId();
    public abstract int getVersion();

    @Override
    public boolean isSimilar(Object o) {
        if (o == null) return false;
        else {
            if (o instanceof OrmBase) {
                OrmBase that = (OrmBase) o;
                if (this.getVersion() == that.getVersion() && Compare.equal(this.getId(), that.getId())) {
                    Class ca = getClass(), cb = o.getClass();
                    // Hibernate may subclass things, so class.equals doesn't work
                    return ca.equals(cb) || ca.getSuperclass().equals(cb) || cb.getSuperclass().equals(ca);
                }
                else return false;
            }
            else return false;
        }
    }
}
