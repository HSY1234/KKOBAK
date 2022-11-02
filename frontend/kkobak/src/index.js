import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { BrowserRouter } from "react-router-dom";
import { ThemeProvider } from "@mui/material/styles";
import "./index.css";
import Theme from "./configs/theme";
import { Provider } from "react-redux";
import Store from "./configs/store";

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  // <React.StrictMode>
  //   <BrowserRouter>
  //     <App />
  //   </BrowserRouter>
  // </React.StrictMode>

  <React.StrictMode>
    <Provider store={Store}>
      <BrowserRouter>
        <ThemeProvider theme={Theme}>
          <App />
        </ThemeProvider>
      </BrowserRouter>
    </Provider>
  </React.StrictMode>

  // <React.StrictMode>
  //   <ThemeProvider theme={Theme}>
  //     <Provider store={store}>
  //       <PersistGate loading={null} persistor={persistor}>
  //         <App />
  //       </PersistGate>
  //     </Provider>
  //   </ThemeProvider>
  // </React.StrictMode>
);
