## Docker Demo
`compose.yaml`
```
services:
  web-gateway:
    image: javaspark/web-gateway
    ports:
      - "0.0.0.0:8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
  inventory-service:
    image: javaspark/inventory-service
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
  account-service:
    image: javaspark/account-service
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
  order-service:
    image: javaspark/order-service
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
  postgres:
    image: javaspark/postgres
    environment:
      - POSTGRES_PASSWORD=postgres
    volumes:
      - "spark-postgres-volume:/var/lib/postgresql"
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U postgres" ]
      interval: 10s
      retries: 5
      start_period: 30s
      timeout: 10s
  rabbitmq:
    image: rabbitmq:4
    hostname: "rmq"
    volumes:
      - "spark-rabbitmq-volume:/var/lib/rabbitmq"
    healthcheck:
      test: [ "CMD-SHELL", "rabbitmq-diagnostics -q ping" ]
      interval: 10s
      retries: 5
      start_period: 30s
      timeout: 10s

volumes:
  spark-postgres-volume:
  spark-rabbitmq-volume:
```

&nbsp;


Type in your terminal:



&nbsp;&nbsp;&nbsp;`curl -o compose.yaml https://raw.githubusercontent.com/796176/Spark/refs/heads/master/compose.yaml`

&nbsp;&nbsp;&nbsp;`sudo docker compose up`

and then open [http://localhost:8080](http://localhost:8080) in your browser.

## Screenshots

![](https://raw.githubusercontent.com/796176/Spark/refs/heads/master/wiki/images/screenshot1.png)
![](https://raw.githubusercontent.com/796176/Spark/refs/heads/master/wiki/images/screenshot2.png)
![](https://raw.githubusercontent.com/796176/Spark/refs/heads/master/wiki/images/screenshot3.png)
![](https://raw.githubusercontent.com/796176/Spark/refs/heads/master/wiki/images/screenshot4.png)
![](https://raw.githubusercontent.com/796176/Spark/refs/heads/master/wiki/images/screenshot5.png)


## Design Overview

![](https://raw.githubusercontent.com/796176/Spark/refs/heads/master/wiki/images/spark_diagram.drawio.png)

![](https://raw.githubusercontent.com/796176/Spark/refs/heads/master/wiki/images/item_deletion_saga.drawio.png)

![](https://raw.githubusercontent.com/796176/Spark/refs/heads/master/wiki/images/placing_order_saga.drawio.png)


## Message Details

- `org.example.spark.account.create-account`

  Creates a new account with roles `USER`
  
  Message version: `1.0`
  
  Parameters:
  - `account_name` - the account's name, type: String
  - `password` - the raw password, type: String

&nbsp;

- `org.example.spark.account.create-admin-account`

  Creates a new account with roles `USER`, `ADMIN`

  Message version: `1.0`

  Parameters:
    - `account_name` - the account's name, type: String
    - `password` - the raw password, type: String

&nbsp;

- `org.example.spark.account.delete-account`

  Deletes the specified account

  Message version: `1.0`

  Parameters:
    - `account_id` - the account's id, type: String

&nbsp;

- `org.example.spark.account.suspend-account`

  Suspends the specified account

  Message version: `1.0`

  Parameters:
    - `account_id` - the account's id, type: String

&nbsp;

- `org.example.spark.account.restore-account`

  Restores the specified account that previously was deleted or suspended

  Message version: `1.0`

  Parameters:
    - `account_id` - the account's id, type: String

&nbsp;

- `org.example.spark.account.get-account`

  Retrieves the specified account

  Message version: `1.0`

  Parameters:
    - `account_id` - the account's id, type: String

  Response properties:
    - `accounts` - the array that contains one account object
    
      Properties of elements of `accounts`:
        - `account_id` - the account's id, type: String
        - `account_name` - the account's name, type: String
        - `roles` - the array of roles, type: Array, element type: String
        - `account_status` - the account's status, type: string

&nbsp;

- `org.example.spark.account.get-accounts`

  Retrieves all the accounts

  Message version: `1.0`

  Response properties:
    - `accounts` - the array that contains zero or more account objects, type: Object
        - `account_id` - the account's id, type: String
        - `account_name` - the account's name, type: String
        - `roles` - the array of roles, type: Array, element type: String
        - `account_status` - the account's status, type: string

&nbsp;

- `org.example.spark.account.change-account-roles`

  Assigns new roles to the specified account

  Message version: `1.0`

  Parameters:
    - `account_id` - the account's id, type: String
    - `roles` - the account's new roles, type: Array, element type: String

&nbsp;

- `org.example.spark.account.get-account-permissions`

  Retrieves various permissions associated with the specified account

  Message version: `1.0`

  Parameters:
    - `account_id` - the account's id, type: String

  Response properties:
    - `authorized_placing_orders` - whether the account is permitted to place new orders, type: Boolean

&nbsp;

- `org.example.spark.inventory.add-item`

  Creates a new inventory item

  Message version: `1.0`

  Parameters:
    - `item_name` - the item's name, type: String
    - `price` - the price, type: Object
      - `currency_amount` - the price integer, type: String
      - `cent_amount` - the decimal part of the price, type: String
    - `amount` - the initial item's amount, type: String
    - `item_picture_name`(optional) - the optional url to the image of item, type: String. It can either a location or an absolute url.

&nbsp;

- `org.example.spark.inventory.delete-item`

  Deletes the item from the inventory

  Message version: `1.0`

  Parameters:
    - `item_id` - the item's id, type: String

&nbsp;

- `org.example.spark.inventory.update-item-amount`

  Assigns a new item amount

  Message version: `1.0`

  Parameters:
    - `item_id` - the item's id, type: String
    - `amount` - the new item amount value, type: String
    - `version` the item's version, type: String

&nbsp;

- `org.example.spark.inventory.get-item`

  Retrieves the specified item

  Message version: `1.0`

  Parameters:
    - `account_id` - the account's id, type: String
  
  Response
    - `items` - the array that contains one item object, type: Array
      - `item_id` - the item's id, type: String
      - `item_name` - the item's name, type: String
      - `item_picture_name`(optional) - the item's image, either in form of a location or absolute url, type: String
      - `version` - the item's version at the moment of retrieval, type: String
      - `price` - the price object, type: Object
        - `currency_amount` - the price integer, type: String
        - `cent_amount` - the decimal part of the price, type: String

&nbsp;

- `org.example.spark.inventory.get-items`

  Retrieves the inventory

  Message version: `1.0`

  Response
    - `items` - the array that contains zero or more item object, type: Array
        - `item_id` - the item's id, type: String
        - `item_name` - the item's name, type: String
        - `item_picture_name`(optional) - the item's image, either in form of a location or absolute url, type: String
        - `version` - the item's version at the moment of retrieval, type: String
        - `price` - the price object, type: Object
            - `currency_amount` - the price integer, type: String
            - `cent_amount` - the decimal part of the price, type: String

&nbsp;

- `org.example.spark.order.place-order`

  Places a new order

  Message version: `1.0`

  Parameters:
    - `account_id` - the placing order account's id, type: String
    - `timestamp` - time when the order was created, type: String
    - `line_items` - the order's line items with at least one object, type: Array
      - `item_id` - the item's id, type: String
      - `amount` - ordered amount of that item, type: String

&nbsp;

- `org.example.spark.order.accept-order`

  Accepts the specified order

  Message version: `1.0`

  Parameters
    - `order_id` - the order's id, type: String
    - `version` - the order's version, type: String

&nbsp;

- `org.example.spark.order.reject-order`

  Rejects the specified order

  Message version: `1.0`

  Parameters
    - `order_id` - the order's id, type: String
    - `version` - the order's version, type: String

&nbsp;

- `org.example.spark.order.cancel-order`

  Cancels the specified order

  Message version: `1.0`

  Parameters
    - `order_id` - the order's id, type: String
    - `version` - the order's version, type: String

&nbsp;

- `org.example.spark.order.restore-order`

  Restores the specified order that was canceled

  Message version: `1.0`

  Parameters
    - `order_id` - the order's id, type: String
    - `version` - the order's version, type: String

&nbsp;

- `org.example.spark.order.get-order`

  Retrieves the specified order

  Message version: `1.0`

  Parameters
    - `order_id` - the order's id, type: String

  Response:
    - `orders` - the array that contains one order object, type: Array
      - `order_id` - the order's id, type: String
      - `account_id` - the id of the account that placed the order
      - `timestamp` - time when order was created
      - `status` - the order's status
      - `version` - the order's version at the moment of retrieval
      - `line_items` - the order's line items with at least one object, type: Array
        - `item_id` - the item's id, type: String
        - `amount` - ordered amount of that item, type: String


&nbsp;

- `org.example.spark.order.get-orders-by-account`

  Retrieves all the order placed by the specified account

  Message version: `1.0`

  Parameters
    - `account_id` - the account's id, type: String

  Response:
    - `orders` - the array that contains zero or more order object, type: Array
        - `order_id` - the order's id, type: String
        - `account_id` - the id of the account that placed the order
        - `timestamp` - time when order was created
        - `status` - the order's status
        - `version` - the order's version at the moment of retrieval
        - `line_items` - the order's line items with at least one object, type: Array
            - `item_id` - the item's id, type: String
            - `amount` - ordered amount of that item, type: String

&nbsp;

- `org.example.spark.order.invalidate-item`

  Forbids to have the specified item as part of an order

  Message version: `1.0`

  Parameters
    - `item_id` - the item's id, type: String


## Event Description

- `org.example.spark.account.account-created`

  Signals creation of a new account
  
  Event version: `1.0`
  
  Properties:
    - `account_id` - the account's id, type: String
    - `encoded_password` - the account's encoded password, type: String
    - `account_name` - the account's name, type: String
    - `roles` - the array that contains account's roles, type: Array, element type: String

&nbsp;

- `org.example.spark.account.account-deleted`

  Signals deletion of an account

  Event version: `1.0`

  Properties:
    - `account_id` - the account's id, type: String

&nbsp;

- `org.example.spark.account.account-suspended`

  Signals suspension of an account

  Event version: `1.0`

  Properties:
    - `account_id` - the account's id, type: String

&nbsp;

- `org.example.spark.account.account-restored`

  Signals restoration of an account

  Event version: `1.0`

  Properties:
    - `account_id` - the account's id, type: String

&nbsp;

- `org.example.spark.account.account-restored`

  Signals restoration of an account

  Event version: `1.0`

  Properties:
    - `account_id` - the account's id, type: String

&nbsp;

- `org.example.spark.account.account-restored`

  Signals restoration of an account

  Event version: `1.0`

  Properties:
    - `account_id` - the account's id, type: String
    - `roles` - the array that contains account's updated roles, type: Array, element type: String

&nbsp;

- `org.example.spark.item-created`

  Signals creation of an item

  Event version: `1.0`

  Properties:
    - `item_id` - the item's id, type: String
    - `item_name` - the item's name, type: String
    - `amount` - the item's initial amount, type: String
    - `price` - the item's price, type: Object
      - `currency_amount` - the price integer, type: String
      - `cent_amount` - the decimal part of the price, type: String

&nbsp;

- `org.example.spark.item-amount-updated`

  Signals change of the item's amount value

  Event version: `1.0`

  Properties:
    - `item_id` - the item's id, type: String
    - `delta` - the difference between the previous and new item's amount values, type: String

- `org.example.spark.item-deleted`

  Signals deletion of an item

  Event version: `1.0`

  Properties:
    - `item_id` - the item's id, type: String

&nbsp;

- `org.example.spark.item-amount-updated`

  Signals change of the item's amount value

  Event version: `1.0`

  Properties:
    - `item_id` - the item's id, type: String
    - `delta` - the difference between the previous and new item's amount values, type: String

&nbsp;

- `org.example.spark.order.order-created`

  Signals creation of an order

  Event version: `1.0`

  Properties:
    - `order_id` - the order's id, type: String
    - `account_id` - the id of the account that placed the order, type: String
    - `timestamp` - time when the order was created, type: String
    - `line_items` - the order's line items with at least one object, type: Array
      - `item_id` - the item's id, type: String
      - `amount` - ordered amount of that item, type: String

&nbsp;

- `org.example.spark.order.order-accepted`

  Signals approval/restoration of an order

  Event version: `1.0`

  Properties:
    - `order_id` - the order's id, type: String

&nbsp;

- `org.example.spark.order.order-rejected`

  Signals rejection of an order

  Event version: `1.0`

  Properties:
    - `order_id` - the order's id, type: String

&nbsp;

- `org.example.spark.order.order-canceled`

  Signals cancellation of an order

  Event version: `1.0`

  Properties:
    - `order_id` - the order's id, type: String

&nbsp;

- `org.example.spark.order.order-canceled`

  Signals cancellation of an order

  Event version: `1.0`

  Properties:
    - `order_id` - the order's id, type: String
