import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet, ImageBackground, TouchableOpacity } from 'react-native';

const LoginScreen = ({ navigation }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = () => {
    // 로그인 로직 추가
    console.log('Login button pressed');
  };

  return (
    <ImageBackground source={require('../../assets/images/l_default.png')} style={styles.background}>
      <Text style={styles.title}>로그인</Text>
      <View style={styles.container}>
        
        
        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>이메일</Text>
          <TextInput
            style={styles.input}
            placeholder="user@example.com"
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
          <TouchableOpacity onPress={() => { /* checkbox toggle 로직 추가 */ }}>
            <View style={styles.checkbox}>
              {/* 체크박스 상태에 따라 스타일 변경 가능 */}
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
  title: {
    fontSize: 24,
    marginBottom: 16,
    textAlign: 'center',
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
    marginRight: 8,
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
    flexDirection: 'colunm',
    alignItems: 'center',
    marginTop: 12,
  },
  link: {
    color: 'blue',
  },
});

export default LoginScreen;
