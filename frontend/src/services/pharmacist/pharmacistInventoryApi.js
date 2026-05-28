import Apis from "../../configs/Apis";
import { mockInventoryTransactions } from "../../data/pharmacistMockData";

export const getInventoryTransactions = async () => {
  // TODO replace mock when backend inventory transaction API is available.
  return Promise.resolve({ data: mockInventoryTransactions });
};

export default Apis;
