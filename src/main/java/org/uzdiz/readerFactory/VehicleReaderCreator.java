package org.uzdiz.readerFactory;

public class VehicleReaderCreator extends CsvReaderCreator {
    public VehicleReaderProduct factoryMethod() {
        return new VehicleReaderProduct();
    }
}
