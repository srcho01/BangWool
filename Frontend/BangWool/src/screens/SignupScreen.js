import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet, ImageBackground, TouchableOpacity } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome';

const SignupScreen = ({ navigation }) => {
  const [name, setName] = useState('');
  const [nickname, setNickname] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [password2, setPassword2] = useState('');
  const [isChecked, setIsChecked] = useState(false);

  const handleLogin = () => {
    console.log('Signup button pressed');
  };

  const toggleCheckbox = () => {
    setIsChecked(!isChecked);
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
            placeholder="김방울"
            value={name}
            onChangeText={setName}
            autoCapitalize="none"
          />
        </View>
        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>닉네임</Text>
          <TextInput
            style={styles.input}
            placeholder="방울이"
            value={nickname}
            onChangeText={setNickname}
            autoCapitalize="none"
          />
        </View>
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
        
        <TouchableOpacity style={styles.button} onPress={handleLogin}>
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

const styles = StyleSheet.create({
  background: {
    flex: 1,
    justifyContent: 'center',
  },
  container: {
    justifyContent: 'center', 
    paddingHorizontal: 16,
    paddingVertical:20,
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
    backgroundColor: 'rgb(91, 115, 133)',
    justifyContent: 'center',
    alignItems: 'center',
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
    flexDirection: 'column',
    alignItems: 'center',
    marginTop: 12,
  },
  link: {
    color: 'blue',
  },
});

export default SignupScreen;
