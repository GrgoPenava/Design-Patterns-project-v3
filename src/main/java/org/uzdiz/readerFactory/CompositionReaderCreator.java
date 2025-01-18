package org.uzdiz.readerFactory;

public class CompositionReaderCreator extends CsvReaderCreator {
    public CsvReaderProduct factoryMethod() {
        return new CompositionReaderProduct();
    }
}
