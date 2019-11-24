package org.isisaddons.module.fakedata.dom;

import org.apache.commons.lang3.RandomUtils;
import org.apache.isis.applib.annotation.Programmatic;

public class Longs extends AbstractRandomValueGenerator{

    public Longs(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public long any() {
        return RandomUtils.nextLong();
    }
}
