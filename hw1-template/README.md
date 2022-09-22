# SERVER API
###Purchase
#### Receives: 
    (String command, String userName, String productName, int quantity) 
#### Example: 
    "purchase thisUser phone 1"
#### Response on Success: 
    (String greeting, int orderId, String userName, String productName, int quantity)
#### Example: 
    "Your order has been placed, 2 thisUser phone 1"
#### Response on Failure:
    (String failureMessage)
#### Example:
    "Not Available - Not enough items"

###Cancel
#### Receives:
    (String command, int orderId)
#### Example:
    "cancel 2"
#### Response on Success:
    (String greeting, int orderId, String signOff)
#### Example:
    "Order 2 is cancelled"
#### Response on Failure:
    (int orderId, String failureMessage)
#### Example:
    "2 not found, no such order"
###Search
#### Receives:
    (String command, String userName)
#### Example:
    "search thisUser"
#### Response on Success:
    (List<Orders.toString()> orders) 
    **** order.toString returns: orderId + ", " + productName + ", "+ quantity ****
#### Example:
    "'2, phone, 1', '7, laptop, 2'"
#### Response on Failure:
    (String failureMessage, String userName)
#### Example:
    "No order found for thisUser"
###List
#### Receives:
    (String command)
#### Example:
    "list"
#### Response on Success:
    (HashMap<String, int> inventory)
#### Example:
    "phone 0 laptop 15 camera 10 ps4 17 xbox 8"