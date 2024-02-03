import * as React from "react";
import * as ReactDOM from "react-dom/client";
import LoginPage from "./routes/LoginPage";
import SignupPage from "./routes/SignupPage";
import HomePage from "./routes/HomePage";
import ProtectedRoute from "./components/ProtectedRoute";
import {
  createBrowserRouter,
  RouterProvider,
} from "react-router-dom";
import "./index.css";
import './custom.scss';

const router = createBrowserRouter([
  {
    path: "/",
    element: <LoginPage />,
  },
  {
    path: "/home",
    element: (
      <ProtectedRoute>
        <HomePage />
      </ProtectedRoute>
    ),
  },
  // Wrap other protected routes similarly
]);

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
