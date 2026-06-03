import { Routes } from "react-router-dom";
import { renderAppRoutes } from "./routes/appRoutes";
import "./App.css";

function App() {
  return <Routes>{renderAppRoutes()}</Routes>;
}

export default App;
