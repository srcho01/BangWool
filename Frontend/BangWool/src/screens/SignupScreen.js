import React, { useState } from 'react';
import { View, Text, TextInput, ImageBackground, TouchableOpacity } from 'react-native';
import { serverUrl } from '@env';
import styles from './styles/SignupStyle';

const SignupScreen = ({ navigation }) => {
  const [name, setName] = useState('');
  const [nickname, setNickname] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [password2, setPassword2] = useState('');
  const [isChecked, setIsChecked] = useState(false);
  const [isNicknameAvailable, setIsNicknameAvailable] = useState(null);
  const [isNicknameConfirmed, setIsNicknameConfirmed] = useState(false);

  const handleSignup = async () => {
    if (password !== password2) {
      alert('Passwords do not match');
      return;
    }
  
    const payload = {
      email,
      password1: password,
      password2,
      name,
      nickname,
      birth: '2000-01-01', // Replace with actual birthdate if needed
    };
  
    try {
      console.log('Sending signup request with payload:', payload); // Log the payload being sent
  
      const response = await fetch(`${serverUrl}auth/signup/local`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });
  
      if (!response.ok) {
        const errorDetails = await response.text(); // Read the response body as text
        console.error(`Signup failed. Status: ${response.status}, Details: ${errorDetails}`);
        throw new Error(`Signup failed. Status: ${response.status}, Details: ${errorDetails}`);
      }
  
      const data = await response.json();
      console.log('Signup successful:', response.data);
      // Handle successful signup, e.g., navigate to another screen or show a success message
    } catch (error) {
      console.error('Error during signup:', error.message); // Log the detailed error message
    }
  };
  

  const toggleCheckbox = () => {
    setIsChecked(!isChecked);
  };
  const checkNicknameAvailability = async () => {
    try {
      console.log('Requesting nickname availability for:', nickname); // Log nickname being checked
      const response = await fetch(`${serverUrl}auth/signup/nickname-check?nickname=${nickname}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
  
      if (!response.ok) {
        const errorDetails = await response.text();
        throw new Error(`Network response was not ok. Status: ${response.status}, Details: ${errorDetails}`);
      }
  
      const data = await response.json();
      console.log('Nickname availability response:', data); // Log response data
      setIsNicknameAvailable(data.data);
      setIsNicknameConfirmed(data.data);
    } catch (error) {
      console.error('Error checking nickname availability:', error.message);
      setIsNicknameAvailable(false);
      setIsNicknameConfirmed(false);
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
          <TextInput
            style={styles.input}
            placeholder="ex) user@example.com"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
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
          disabled={!isNicknameConfirmed || !name || !email || !password || !password2}
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
