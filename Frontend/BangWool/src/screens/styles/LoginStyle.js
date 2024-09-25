import { StyleSheet } from 'react-native';
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
  export default styles;
  
  