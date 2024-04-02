package org.parser;

public class Currency {
    private String name;
    private String price;
    private String marketCap;

    public Currency() {}

    public Currency(String name, String price, String marketCap) {
        this.name = name;
        this.price = price;
        this.marketCap = marketCap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }
}
