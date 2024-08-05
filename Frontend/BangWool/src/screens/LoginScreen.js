import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet, ImageBackground, TouchableOpacity, Alert, KeyboardAvoidingView, Platform, ScrollView } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome';
import { serverUrl } from '@env';
import styles from './styles/LoginStyle';

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
  
      const response = await fetch(`${serverUrl}login`, { 
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });
  
      let errorDetails = '';
      if (!response.ok) {
        errorDetails = await response.text(); // Attempt to read error details
        console.error(`Login failed. Status: ${response.status}, Details: ${errorDetails}`);
  
        if (response.status === 401) {
          Alert.alert('Login Error', 'Unauthorized: ' + (errorDetails || 'Invalid credentials.'));
        } else if (response.status === 403) {
          Alert.alert('Login Error', 'Forbidden: ' + (errorDetails || 'Access denied.'));
        } else {
          Alert.alert('Login Error', errorDetails || 'An error occurred');
        }
        return;
      }
  
      const data = await response.json();
      console.log('Login successful:', data);
  
      // Save tokens and navigate
      // await AsyncStorage.setItem('accessToken', data.data.accessToken);
      // await AsyncStorage.setItem('refreshToken', data.data.refreshToken);
      // navigation.navigate('Home');
  
    } catch (error) {
      console.error('Error during login:', error.message);
      Alert.alert('Login Error', 'An unexpected error occurred');
    }
  };
  

  const toggleCheckbox = () => {
    setIsChecked(!isChecked);
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      style={{ flex: 1 }}
    >
      <ScrollView contentContainerStyle={{ flexGrow: 1 }}>
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
              <TouchableOpacity onPress={() => navigation.navigate('Signup')}>
                <Text style={styles.link}>아이디가 없으세요? 회원가입하기</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={() => navigation.navigate('ForgotPassword')}>
                <Text style={styles.link}>비밀번호찾기</Text>
              </TouchableOpacity>
            </View>
          </View>
        </ImageBackground>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

export default LoginScreen;
