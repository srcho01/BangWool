import React, { useState } from 'react';
import { View, Text, TextInput, ImageBackground, TouchableOpacity, Alert } from 'react-native';
import { serverUrl } from '@env';
import styles from './styles/SignupStyle';

const SignupScreen = ({ navigation }) => {
  const [name, setName] = useState('');
  const [nickname, setNickname] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [password2, setPassword2] = useState('');
  const [birthdate, setBirthdate] = useState(''); // State for birthdate
  const [isNicknameAvailable, setIsNicknameAvailable] = useState(null);
  const [isNicknameConfirmed, setIsNicknameConfirmed] = useState(false);
  const [verificationCode, setVerificationCode] = useState('');
  const [isCodeSent, setIsCodeSent] = useState(false);
  const [isEmailVerified, setIsEmailVerified] = useState(false);

  const parseBirthdate = (input) => {
    if (input.length !== 8 || isNaN(input)) {
      return null; // Invalid input length or not a number
    }

    const year = input.substring(0, 4);
    const month = input.substring(4, 6);
    const day = input.substring(6, 8);

    // Validate month and day
    if (month < 1 || month > 12 || day < 1 || day > 31) {
      return null;
    }

    const date = new Date(`${year}-${month}-${day}`);
    if (date.getFullYear() !== parseInt(year) || date.getMonth() + 1 !== parseInt(month) || date.getDate() !== parseInt(day)) {
      return null; // Invalid date
    }

    return `${year}-${month}-${day}`; // Return in YYYY-MM-DD format
  };

  const handleSignup = async () => {
    if (password !== password2) {
      Alert.alert('Passwords do not match');
      return;
    }

    const formattedBirthdate = parseBirthdate(birthdate);
    if (!formattedBirthdate) {
      Alert.alert('Validation Error', 'Please enter a valid birthdate in YYYYMMDD format');
      return;
    }

    const payload = {
      email,
      password1: password,
      password2,
      name,
      nickname,
      birth: formattedBirthdate, // Include formatted birthdate in the payload
    };

    try {
      console.log('Sending signup request with payload:', payload);

      const response = await fetch(`${serverUrl}auth/signup/local`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorDetails = await response.text();
        console.error(`Signup failed. Status: ${response.status}, Details: ${errorDetails}`);
        throw new Error(`Signup failed. Status: ${response.status}, Details: ${errorDetails}`);
      }

      const data = await response.json();
      console.log('Signup successful:', data);

      Alert.alert(
        'Signup Successful',
        'Please check your email to verify your account before logging in.',
        [{ text: 'OK', onPress: () => navigation.navigate('Login') }]
      );
    } catch (error) {
      console.error('Error during signup:', error.message);
    }
  };

  const checkNicknameAvailability = async () => {
    try {
      console.log('Requesting nickname availability for:', nickname);
      const response = await fetch(`${serverUrl}auth/nickname-check?nickname=${nickname}`, {
        method: 'GET',
      });

      if (!response.ok) {
        const errorDetails = await response.text();
        throw new Error(`Network response was not ok. Status: ${response.status}, Details: ${errorDetails}`);
      }

      const data = await response.json();
      console.log('Nickname availability response:', data);
      setIsNicknameAvailable(data.data);
      setIsNicknameConfirmed(data.data);
    } catch (error) {
      console.error('Error checking nickname availability:', error.message);
      setIsNicknameAvailable(false);
      setIsNicknameConfirmed(false);
    }
  };

  const sendVerificationCode = async () => {
    try {
      const payload = { email };
      console.log('Sending verification code to:', email);

      const response = await fetch(`${serverUrl}auth/email/send`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorDetails = await response.text();
        throw new Error(`Failed to send verification code. Status: ${response.status}, Details: ${errorDetails}`);
      }

      const data = await response.json();
      console.log('Verification code sent:', data);
      setIsCodeSent(true);
      Alert.alert('Verification Code Sent', 'Please check your email for the verification code.');
    } catch (error) {
      console.error('Error sending verification code:', error.message);
    }
  };

  const verifyEmailCode = async () => {
    try {
      const payload = { email, code: verificationCode };
      console.log('Verifying email with payload:', payload);

      const response = await fetch(`${serverUrl}auth/email/check`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorDetails = await response.text();
        throw new Error(`Failed to verify email. Status: ${response.status}, Details: ${errorDetails}`);
      }

      const data = await response.json();
      console.log('Email verified:', data);
      setIsEmailVerified(true);
      Alert.alert('Email Verified', 'Your email has been successfully verified.');
    } catch (error) {
      console.error('Error verifying email:', error.message);
    }
  };

  return (
    <ImageBackground source={require('../../assets/images/l_default.png')} style={styles.background}>
      <Text style={styles.com}>방울</Text>
      <Text style={styles.title}>회원가입</Text>
      <View style={styles.container}>
        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>이름</Text>
          <TextInput
            style={styles.input}
            placeholder="ex) 김방울"
            value={name}
            onChangeText={setName}
            autoCapitalize="none"
          />
        </View>
        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>닉네임</Text>
          <View style={styles.nicknameContainer}>
            <TextInput
              style={[styles.input, { opacity: isNicknameConfirmed ? 0.5 : 1 }]}
              placeholder="ex) 방울이"
              value={nickname}
              onChangeText={setNickname}
              autoCapitalize="none"
              editable={!isNicknameConfirmed}
            />
            <TouchableOpacity
              style={styles.checkButton}
              onPress={checkNicknameAvailability}
              disabled={isNicknameConfirmed}
            >
              <Text style={styles.checkButtonText}>중복확인</Text>
            </TouchableOpacity>
          </View>
          {isNicknameAvailable === false && <Text style={styles.errorText}>닉네임이 이미 사용 중입니다.</Text>}
          {isNicknameAvailable === true && <Text style={styles.successText}>닉네임을 사용할 수 있습니다.</Text>}
        </View>
        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>이메일</Text>
          <View style={styles.emailContainer}>
            <TextInput
              style={[styles.input, { opacity: isCodeSent ? 0.5 : 1 }]}
              placeholder="ex) user@example.com"
              value={email}
              onChangeText={setEmail}
              keyboardType="email-address"
              autoCapitalize="none"
              editable={!isCodeSent}
            />
            <TouchableOpacity
              style={styles.sendButton}
              onPress={sendVerificationCode}
              disabled={isCodeSent}
            >
              <Text style={styles.checkButtonText}>인증번호 보내기</Text>
            </TouchableOpacity>
          </View>
        </View>
        {isCodeSent && (
          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>인증번호</Text>
            <TextInput
              style={styles.input}
              placeholder="인증번호 입력"
              value={verificationCode}
              onChangeText={setVerificationCode}
              autoCapitalize="none"
            />
            <TouchableOpacity
              style={styles.verifyButton}
              onPress={verifyEmailCode}
              disabled={isEmailVerified}
            >
              <Text style={styles.verifyButtonText}>인증</Text>
            </TouchableOpacity>
          </View>
        )}
        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>생년월일 (YYYYMMDD)</Text>
          <TextInput
            style={styles.input}
            placeholder="ex) 20240101"
            value={birthdate}
            onChangeText={setBirthdate}
            keyboardType="numeric"
            maxLength={8}
          />
        </View>
        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>비밀번호</Text>
          <TextInput
            style={styles.input}
            placeholder="비밀번호 입력"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
          />
        </View>
        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>비밀번호확인</Text>
          <TextInput
            style={styles.input}
            placeholder="비밀번호 입력"
            value={password2}
            onChangeText={setPassword2}
            secureTextEntry
          />
        </View>
        <TouchableOpacity
          style={styles.button}
          onPress={handleSignup}
          disabled={!isNicknameConfirmed || !name || !isEmailVerified || !email || !password || !password2 || !birthdate}
        >
          <Text style={styles.buttonText}>회원가입</Text>
        </TouchableOpacity>
        <View style={styles.footer}>
          <TouchableOpacity onPress={() => navigation.navigate('Login')}>
            <Text style={styles.link}>아이디가 있어요! 로그인하기</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={() => navigation.navigate('ForgotPassword')}>
            <Text style={styles.link}>비밀번호찾기</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ImageBackground>
  );
};

export default SignupScreen;
