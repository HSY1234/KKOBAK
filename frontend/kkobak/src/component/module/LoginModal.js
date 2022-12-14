import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Box, styled } from "@mui/system";
import { Modal } from "@mui/material";

import CloseIcon from "@mui/icons-material/Close";
import Logo from "../../static/Logo.png";
import Input from "component/atom/Input";
import TextButton from "component/atom/TextButton";
import Text from "component/atom/Text";

import storage from "helper/storage";

import { requestLogin } from "api/userApi";

const ModalStyle = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: "50%",
  height: "55%",
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
  `
);

export default function LoginModal() {
  const navigate = useNavigate();

  const [open, setOpen] = React.useState(true);
  const handleClose = () => setOpen(false);

  const [email, setEmail] = useState("");
  const [passwd, setPasswd] = useState("");
  const [loginMessage, setLoginMessage] = useState("");

  function goToSignup() {
    navigate("/signup");
  }

  function onChangeEmail(e) {
    setEmail(e.target.value);
  }

  function onChangePasswd(e) {
    setPasswd(e.target.value);
  }

  function loginSuccess(res) {
    const accessToken = res.data.accessToken;
    storage.set("accessToken", accessToken);

    setEmail("");
    setPasswd("");

    setLoginMessage("로그인 완료");
    navigate("/");

    window.location.reload();
  }
  function loginFail(res) {
    setLoginMessage("이메일 또는 비밀번호를 잘못 입력했습니다.");
    setTimeout(() => setLoginMessage(""), 1500);
  }
  function onClickLogin() {
    if (!email) {
      setLoginMessage("이메일을 입력해주세요.");
      setTimeout(() => setLoginMessage(""), 1500);
      return;
    }

    if (!passwd) {
      setLoginMessage("비밀번호를 입력해주세요.");
      setTimeout(() => setLoginMessage(""), 1500);
      return;
    }

    requestLogin(email, passwd, loginSuccess, loginFail);
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
                placeholder="이메일"
                onChange={onChangeEmail}
              ></Input>
            </InputBox>
          </ContentBox>

          <ContentBox>
            <InputBox>
              <Input
                type="password"
                placeholder="비밀번호"
                onChange={onChangePasswd}
              ></Input>
            </InputBox>
          </ContentBox>

          <Box onClick={onClickLogin} sx={{ mt: "30px" }}>
            <TextButton size="m" my="15">
              로그인
            </TextButton>
          </Box>
          <Box onClick={goToSignup} sx={{ mt: "10px" }}>
            <TextButton size="m" color="secondary" my="5">
              회원가입
            </TextButton>
          </Box>

          <Text size="s">{loginMessage}</Text>
        </Box>
      </Box>
    </Modal>
  );
}
