package org.uzdiz.readerFactory;

public class DrivingDaysReaderCreator extends CsvReaderCreator {
    @Override
    public CsvReaderProduct factoryMethod() {
        return new DrivingDaysReaderProduct();
    }
}
