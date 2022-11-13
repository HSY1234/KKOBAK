import React from "react";
import { Routes, Route } from "react-router-dom";
import Main from "../component/page/Main";
import MyChallenge from "../component/page/MyChallenge";
import Setting from "component/page/Setting";
import storage from "../helper/storage";
import LoginModal from "component/module/LoginModal";
import SignupModal from "component/module/SignupModal";
import ChallengeDetail from "component/page/ChallengeDetail";
import GroupChallengeDetail from "component/page/GroupChallengeDetail";
import ChallengeRegister from "component/page/ChallengeRegister";
import Statistics from "component/page/Statistics";
import IntroPage from "component/page/IntroPage";
import GroupChallenge from "component/page/GroupChallenge";
import GroupChallengeRegister from "component/page/GroupChallengeRegister";

function checkAuth() {
  return !!storage.get("accessToken");
}

function CheckAuth({ children }) {
  if (checkAuth()) return children;
  return <LoginModal />;
}

export default function RouterConfiguration() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <CheckAuth>
            <Main />
          </CheckAuth>
        }
      />
      <Route
        path="/myChallenge"
        element={
          <CheckAuth>
            <MyChallenge />
          </CheckAuth>
        }
      />
      <Route
        path="/GroupChallenge"
        element={
          <CheckAuth>
            <GroupChallenge />
          </CheckAuth>
        }
      />
      <Route
        path="/register"
        element={
          <CheckAuth>
            <ChallengeRegister />
          </CheckAuth>
        }
      /><Route
      path="/group/register"
      element={
        <CheckAuth>
          <GroupChallengeRegister />
        </CheckAuth>
      }
    />
      <Route
        path="/myChallenge/:chlId"
        element={
          <CheckAuth>
            <ChallengeDetail />
          </CheckAuth>
        }
      />
      <Route
        path="/GroupChallenge/:chlId"
        element={
          <CheckAuth>
            <GroupChallengeDetail />
          </CheckAuth>
        }
      />
      <Route
        path="/Statistics/:chlId"
        element={
          <CheckAuth>
            <Statistics />
          </CheckAuth>
        }
      />
      <Route path="/Setting" element={<Setting />} />
      <Route path="/signup" element={<SignupModal />} />
      <Route path="/IntroPage" element={<IntroPage />} />
    </Routes>
  );
}
