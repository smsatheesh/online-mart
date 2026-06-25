const CATEGORY = {
  id: "string",
  name: "string",
  status: "boolean",

  created_by: "string",
  created_at: "timestamp",
  updated_by: "string",
  updated_at: "timestamp",
};

const PRODUCT = {
  id: "string",
  category_id: "string",
  name: "string",
  description: "string",
  price: "integer",
  stock_quantity: "integer",
  thumbnail_url: "string",
  status: "boolean",

  created_by: "string",
  created_at: "timestamp",
  updated_by: "string",
  updated_at: "timestamp",
};

const CART = {
  id: "string",
  customer_id: "string",
  platform: "string",
  status: boolean,

  created_by: "string",
  created_at: "timestamp",
  updated_by: "string",
  updated_at: "timestamp",
};

const CART_ITEMS = {
  id: "string",
  cart_id: "string",
  product_id: "string",
  quantity: "integer",
  unit_price: "integer",

  created_by: "string",
  created_at: "timestamp",
  updated_by: "string",
  updated_at: "timestamp",
};

const ORDER = {
  id: "string",
  customer_id: "string",
  cart_id: "string",
  status: "string",
  total_amount: "integer",

  created_by: "string",
  created_at: "timestamp",
  updated_by: "string",
  updated_at: "timestamp",
};

const SHIPPING_DETAILS = {
  id: "string",
  customer_id: "string",
  order_id: "string",
  line1: "string",
  city: "string",
  state: "string",
  postal_code: "integer",

  created_by: "string",
  created_at: "timestamp",
  updated_by: "string",
  updated_at: "timestamp",
};

const ORDER_ITEMS = {
  id: "string",
  order_id: "string",
  product_id: "string",
  quantity: "integer",

  created_by: "string",
  created_at: "timestamp",
  updated_by: "string",
  updated_at: "timestamp",
};
