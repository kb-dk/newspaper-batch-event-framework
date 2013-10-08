package dk.statsbiblioteket.newspaper.processmonitor.datasources;

public enum EventID {

    Initial,
    Added_to_shipping_container,
    Shipped_to_supplier,
    Data_Received,
    Data_Archived,
    Structure_Checked,


    Shipped_from_supplier,
    Received_from_supplier,
    FollowUp,
    Approved;

}
