package org.uzdiz.readerFactory;

public abstract class CsvReaderCreator {
    private CsvReaderProduct csvReaderProduct;

    public abstract CsvReaderProduct factoryMethod();

    public void loadData(String filePath) {
        csvReaderProduct = factoryMethod();
        csvReaderProduct.loadData(filePath);
    }
}
