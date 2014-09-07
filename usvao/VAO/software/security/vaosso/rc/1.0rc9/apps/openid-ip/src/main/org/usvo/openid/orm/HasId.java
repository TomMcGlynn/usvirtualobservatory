package org.usvo.openid.orm;

import java.io.Serializable;

public interface HasId extends Serializable {
    Long getId();
}