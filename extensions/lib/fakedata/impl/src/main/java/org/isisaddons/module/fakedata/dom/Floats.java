package org.isisaddons.module.fakedata.dom;

import org.apache.commons.lang3.RandomUtils;
import org.apache.isis.applib.annotation.Programmatic;

public class Floats extends AbstractRandomValueGenerator{

    public Floats(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public float any() {
        return RandomUtils.nextFloat();
    }
}
