import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Box, styled } from "@mui/system";
import { Modal } from "@mui/material";

import CloseIcon from "@mui/icons-material/Close";
import Logo from "static/Logo.png";
import Input from "component/atom/Input";
import TextButton from "component/atom/TextButton";
import Text from "component/atom/Text";

import storage from "helper/storage";

import {
  requestJoin,
  requestEmailCheck,
  requestNicknameCheck,
  requestAuthNum,
  requestPasswdCheck,
} from "api/userApi";

const ModalStyle = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: "50%",
  height: "90%",
  bgcolor: "white",
  borderRadius: "10px",
  border: "none",
};

const LogoStyle = {
  bgcolor: "#E8F2F9",
  width: "100%",
  height: "15%",
  my: 5,
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
};

const ContentBox = styled(Box)(
  () => `
  height: 50px; 
  display: flex;
  align-items: center;
  justify-content: start;
  `
);

const InputBox = styled(Box)(
  () => `
  width: 400px;
  height: 40px;
  margin: 0 10px 0 100px;
  `
);

export default function SignupModal() {
  const navigate = useNavigate();

  const [open, setOpen] = React.useState(true);
  const handleClose = () => setOpen(false);

  const [email, setEmail] = useState("");
  const [passwd, setPasswd] = useState("");
  const [passwdCheck, setPasswdCheck] = useState("");
  const [nickname, setNickname] = useState("");
  const [phoneNum, setPhoneNum] = useState("");
  const [authNum, setAuthNum] = useState("");
  const [confirmEmail, setConfirmEmail] = useState("");
  const [confirmPasswd, setConfirmPasswd] = useState("");
  const [confirmNickname, setConfirmNickname] = useState("");
  const [confirmPhoneNum, setConfirmPhoneNum] = useState("");
  const [confirmAuthNum, setConfirmAuthNum] = useState("");
  const [signupMessage, setSignupMessage] = useState("");

  function goToLogin() {
    navigate("/login");
  }

  function onChangeEmail(e) {
    setEmail(e.target.value);
    setConfirmEmail("");
  }

  function onChangePasswd(e) {
    setPasswd(e.target.value);
    setConfirmPasswd("");
  }

  function onChangePasswdCheck(e) {
    setPasswdCheck(e.target.value);
    setConfirmPasswd("");
  }

  function onChangeNickname(e) {
    setNickname(e.target.value);
    setConfirmNickname("");
  }

  function onChangePhoneNum(e) {
    setPhoneNum(e.target.value);
    setConfirmPhoneNum("");
  }

  function onChangeAuthNum(e) {
    setAuthNum(e.target.value);
  }

  // ????????????
  function joinSuccess(res) {
    const accessToken = res.data.accessToken;
    storage.set("accessToken", accessToken);

    setEmail("");
    setPasswd("");
    setPasswdCheck("");
    setNickname("");
    setPhoneNum("");
    setAuthNum("");
    setConfirmEmail("");
    setConfirmPasswd("");
    setConfirmNickname("");
    setConfirmPhoneNum("");
    setConfirmAuthNum("");

    setSignupMessage("???????????? ??????");
  }
  function joinFail(res) {
    // console.log("Signup Fail", res);
  }
  function onClickSignup() {
    if (!email) {
      setSignupMessage("???????????? ??????????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
      return;
    }

    if (!confirmEmail) {
      setSignupMessage("????????? ?????? ????????? ????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
      return;
    }

    if (!passwd || !passwdCheck) {
      setSignupMessage("??????????????? ??????????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
      return;
    }

    if (!nickname) {
      setSignupMessage("???????????? ??????????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
      return;
    }

    if (!confirmNickname) {
      setSignupMessage("????????? ?????? ????????? ????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
      return;
    }

    if (!phoneNum) {
      setSignupMessage("??????????????? ??????????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
      return;
    }

    if (!authNum) {
      setSignupMessage("??????????????? ??????????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
      return;
    }

    onClickPasswdCheck();

    if (confirmPasswd && onClickAuthNumCheck()) {
      requestJoin(
        confirmEmail,
        confirmPhoneNum,
        confirmNickname,
        passwdCheck,
        joinSuccess,
        joinFail
      );
    }
  }

  // ????????? ?????? ??????
  function emailCheckSuccess(res) {
    if (res.data) {
      setConfirmEmail(email);
      setSignupMessage("?????? ????????? ??????????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
    }
  }
  function emailCheckFail(res) {
    setConfirmEmail("");
    setSignupMessage("?????? ???????????? ??????????????????.");
    setTimeout(() => setSignupMessage(""), 1500);
    // console.log("Email Check Fail", res);
  }
  function onClickEmailCheck() {
    requestEmailCheck(email, emailCheckSuccess, emailCheckFail);
  }

  // ???????????? ??????
  function passwdCheckSuccess(res) {
    if (res.data) {
      setConfirmPasswd(passwd);
    } else {
      setSignupMessage(
        "?????????, ??????, ????????????($`~!@$!%*#^?&()_=+)??? ????????? 8-20?????? ??????????????????."
      );
      setTimeout(() => setSignupMessage(""), 1500);
    }
  }
  function passwdCheckFail(res) {
    // console.log("Password Check Fail", res);
  }
  function onClickPasswdCheck() {
    if (passwd !== passwdCheck) {
      setSignupMessage("??????????????? ???????????? ????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
    } else {
      requestPasswdCheck(passwd, passwdCheckSuccess, passwdCheckFail);
    }
  }

  // ????????? ?????? ??????
  function nicknameCheckSuccess(res) {
    if (res.data) {
      setConfirmNickname(nickname);
      setSignupMessage("?????? ????????? ??????????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
    } else {
      setSignupMessage("?????? ???????????? ??????????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
    }
  }
  function nicknameCheckFail(res) {
    // console.log("Nickname Check Fail", res);
  }
  function onClickNicknameCheck() {
    if (nickname !== "") {
      requestNicknameCheck(nickname, nicknameCheckSuccess, nicknameCheckFail);
    } else {
      setSignupMessage("???????????? ??????????????????.");
    }
  }

  // ???????????? ??????
  function sendAuthNumSuccess(res) {
    setConfirmPhoneNum(phoneNum);
    setConfirmAuthNum(res.data.authNum);
    console.log("???????????? : " + res.data.authNum);
  }
  function sendAuthNumFail(res) {
    setSignupMessage(res.response.data.message);
  }
  function onClickSendAuthNum() {
    requestAuthNum(phoneNum, sendAuthNumSuccess, sendAuthNumFail);
  }

  // ???????????? ??????
  function onClickAuthNumCheck() {
    if (authNum === confirmAuthNum) {
      return true;
    } else {
      setSignupMessage("????????? ?????????????????????.");
      setTimeout(() => setSignupMessage(""), 1500);
    }
  }

  return (
    <Modal open={open} onClose={handleClose}>
      <Box sx={ModalStyle}>
        <CloseIcon
          onClick={handleClose}
          sx={{ m: 1, float: "right" }}
        ></CloseIcon>
        <Box sx={LogoStyle}>
          <img alt="logo" src={Logo} height="80%" />
        </Box>

        <Box
          sx={{
            width: "100%",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <ContentBox>
            <InputBox>
              <Input
                type="text"
                placeholder="?????????"
                onChange={onChangeEmail}
              ></Input>
            </InputBox>
            <Box onClick={onClickEmailCheck} sx={{ width: "90px" }}>
              <Text size="13px" weight="medium" color="grey">
                ?????? ??????
              </Text>
            </Box>
          </ContentBox>

          <ContentBox>
            <InputBox>
              <Input
                type="password"
                placeholder="????????????"
                onChange={onChangePasswd}
              ></Input>
            </InputBox>
            <Box sx={{ width: "90px" }} />
          </ContentBox>

          <ContentBox>
            <InputBox>
              <Input
                type="password"
                placeholder="???????????? ?????????"
                onChange={onChangePasswdCheck}
              ></Input>
            </InputBox>
            <Box sx={{ width: "90px" }} />
          </ContentBox>

          <ContentBox>
            <InputBox>
              <Input
                type="text"
                placeholder="?????????"
                onChange={onChangeNickname}
              ></Input>
            </InputBox>
            <Box onClick={onClickNicknameCheck} sx={{ width: "90px" }}>
              <Text size="13px" weight="medium" color="grey">
                ?????? ??????
              </Text>
            </Box>
          </ContentBox>

          <ContentBox>
            <InputBox>
              <Input
                type="text"
                placeholder="????????????"
                onChange={onChangePhoneNum}
              ></Input>
            </InputBox>
            <Box onClick={onClickSendAuthNum} sx={{ width: "90px" }}>
              <Text size="13px" weight="medium" color="grey">
                ???????????? ??????
              </Text>
            </Box>
          </ContentBox>

          <ContentBox>
            <InputBox>
              <Input
                type="text"
                placeholder="????????????"
                onChange={onChangeAuthNum}
              ></Input>
            </InputBox>
            <Box sx={{ width: "90px" }}></Box>
          </ContentBox>

          <Box onClick={onClickSignup} sx={{ mt: "30px" }}>
            <TextButton size="m" my="15">
              ????????????
            </TextButton>
          </Box>
          <Box onClick={goToLogin} sx={{ mt: "10px" }}>
            <TextButton size="m" color="secondary" my="5">
              ?????????
            </TextButton>
          </Box>

          <Text>{signupMessage}</Text>
        </Box>
      </Box>
    </Modal>
  );
}
