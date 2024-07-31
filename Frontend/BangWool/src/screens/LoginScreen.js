import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet, ImageBackground, TouchableOpacity, Alert } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome';
import { serverUrl } from '@env';

const LoginScreen = ({ navigation }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isChecked, setIsChecked] = useState(false);
  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert('Validation Error', 'Please enter both email and password');
      return;
    }
  
    const payload = {
      email,
      password,
    };
  
    try {
      console.log('Sending login request with payload:', payload);
  
      const response = await fetch(`${serverUrl}/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });
  
      // 응답 상태 코드 확인
      if (!response.ok) {
        const errorDetails = await response.text(); // 응답 본문을 텍스트로 읽어봅니다
        console.error(`Login failed. Status: ${response.status}, Details: ${errorDetails}`);
        Alert.alert('Login Error', errorDetails || 'An error occurred');
        return;
      }
  
      // 응답 본문을 JSON으로 파싱합니다
      const data = await response.json();
      console.log('Login successful:', data);
  
      // 로그인 성공 처리
      // 예를 들어, AsyncStorage에 토큰 저장 및 다음 화면으로 이동
      // await AsyncStorage.setItem('accessToken', data.data.accessToken);
      // await AsyncStorage.setItem('refreshToken', data.data.refreshToken);
      // navigation.navigate('Home'); // 'Home'을 원하는 화면으로 변경하세요
  
    } catch (error) {
      console.error('Error during login:', error.message);
      Alert.alert('Login Error', 'An unexpected error occurred');
    }
  };
  

  const toggleCheckbox = () => {
    setIsChecked(!isChecked);
  };

  return (
    <ImageBackground source={require('../../assets/images/l_default.png')} style={styles.background}>
      <Text style={styles.com}>방울</Text>
      <Text style={styles.title}>로그인</Text>
      <View style={styles.container}>
        
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
        
        <View style={styles.checkboxContainer}>
          <TouchableOpacity onPress={toggleCheckbox}>
            <View style={[styles.checkbox, isChecked && styles.checkboxChecked]}>
              {isChecked && <Icon name="check" size={16} color="white" />}
            </View>
          </TouchableOpacity>
          <Text style={styles.checkboxLabel}>로그인 유지하기</Text>
        </View>
        
        <TouchableOpacity style={styles.button} onPress={handleLogin}>
          <Text style={styles.buttonText}>로그인</Text>
        </TouchableOpacity>
        
        <View style={styles.footer}>
          <TouchableOpacity onPress={() => navigation.navigate('SignUp')}>
            <Text style={styles.link}>아이디가 없으세요? 회원가입하기</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={() => navigation.navigate('ForgotPassword')}>
            <Text style={styles.link}>비밀번호찾기</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ImageBackground>
  );
};

const styles = StyleSheet.create({
  background: {
    flex: 1,
    justifyContent: 'center',
  },
  container: {
    flex: 0.5,
    justifyContent: 'center',
    paddingHorizontal: 16,
    marginHorizontal: 16,
    borderRadius: 10,
    backgroundColor: 'rgba(232, 232, 232, 0.8)',
  },
  com: {
    position: 'absolute',
    top: 16,
    right: 16, 
    fontSize: 24,
    fontFamily: 'KCC-Hanbit',
  },
  title: {
    fontSize: 24,
    marginBottom: 16,
    textAlign: 'left', 
    marginLeft: 20, 
  },
  inputContainer: {
    marginBottom: 12,
  },
  inputLabel: {
    fontSize: 16,
    marginBottom: 4,
  },
  input: {
    height: 40,
    borderColor: 'gray',
    paddingHorizontal: 8,
    borderRadius: 8,
    backgroundColor: 'rgb(253, 253, 253)',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  checkboxContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderWidth: 1,
    borderRadius: 8,
    borderColor: 'gray',
    backgroundColor: 'rgb(253, 253, 253)',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 8,
  },
  checkboxChecked: {
    backgroundColor: 'rgb(91, 115, 133)',
  },
  checkboxLabel: {
    fontSize: 14,
  },
  button: {
    backgroundColor: 'rgb(91, 115, 133)',
    padding: 10,
    borderRadius: 8,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
  },
  footer: {
    flexDirection: 'column',
    alignItems: 'center',
    marginTop: 12,
  },
  link: {
    color: 'blue',
  },
});

export default LoginScreen;
