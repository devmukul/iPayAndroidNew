package bd.com.ipay.ipayskeleton.Model.MMModule.MakePayment;


public class InvoiceItemList {
    private String itemDescription;
    private String itemName;
    private int quantity;
    private int rate;
    private int totalPrice;

    public InvoiceItemList(String itemDescription, String itemName, int quantity, int rate, int totalPrice) {
        this.itemDescription = itemDescription;
        this.itemName = itemName;
        this.quantity = quantity;
        this.rate = rate;
        this.totalPrice = totalPrice;
    }
}