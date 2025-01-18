package org.uzdiz.readerFactory;

public class TimeTableReaderCreator extends CsvReaderCreator {
    @Override
    public CsvReaderProduct factoryMethod() {
        return new TimeTableReaderProduct();
    }
}
