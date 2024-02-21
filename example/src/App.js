import React, { useEffect, useState } from 'react';
import { Image, NativeModules, StatusBar } from 'react-native';
import { FlatList } from 'react-native';
import { Button, DeviceEventEmitter, StyleSheet, Text, View } from 'react-native';
import QuickActions from "react-native-quick-actions";
import {
  addShortcutToScreen,
  exists,
  removeShortcut,
  addPinnedShortcut,
  addShortcut,
  checkPermissionToCall,
  updateShortcut,
  addDetailToShortcut,
  getDrawableImageNames
} from 'react-native-shortcuts-launcher';

const App = () => {
  const [images, setImages] = useState([]);

  useEffect(() => {
    DeviceEventEmitter.addListener("quickActionShortcut", data => {
      console.log(data.title);
      console.log(data.type);
      console.log(data.userInfo);
    });
  }, []);

  const checkPermission = () => {
    checkPermissionToCall();
  };

  const addedShortCut = () => {
    addDetailToShortcut({
      id: 'id2',
      shortLabel: 'sample',
      longLabel: 'sample label',
      iconFolderName: 'mipmap',
      iconName: 'icon_launcher'
    });
  };

  const createShortCut = () => {
    addShortcutToScreen({
      id: Date.now().toString(),
      phoneNumber: '01542999120288',
      shortLabel: 'BAGUA',
      longLabel: 'Bagua shortcut',
      iconFolderName: 'drawable',
      iconName: 'icon_launcher.png'
    }).then(function (e) {
      console.log('CREATE SHORTCUTS => ', e);
    }).catch(function (err) {
      console.log('ERRO CREATE SHORTCUTS => ', err);
    });
  };

  const pinnedShortcut = () => {
    const shortcuts = {
      id: Date.now().toString(),
      iconBitmap: "null",
      phone: '01542999120288',
      shortLabel: 'Short Label',
      longLabel: 'Long label',
      iconFolderName: 'drawable',
      iconName: 'axe',
      intent: {
        action: 'android.intent.action.CALL',
        // "categories": [
        // 'android.intent.category.CALL', // Built-in Android category
        // 'MY_CATEGORY' // Custom categories are also supported
        // ],
        flags: 'FLAG_ACTIVITY_CLEAR_TOP',
        // "data": 'myapp://telefones_uteis/index.html?param=value', // Must be a well-formed URI
        // "extras": {
        //   'android.intent.extra.SUBJECT': 'Hello world!', // Built-in Android extra (string)
        //   'nro_fone': '99999999999', // Custom extras are also supported (boolean, number and string only)
        // }
        phone: '01542999339947', // Custom extras are also supported (boolean, number and string only)
      },
    };
    // const jsonString = JSON.stringify(shortcuts);
    // const jsonArray = new JSONArray(jsonString);

    addPinnedShortcut(JSON.stringify(shortcuts)).then(function (e) {
      console.log('ADD PINNED SHORTCUTS => ', e);
    }).catch(function (err) {
      console.log('ERRO PINNED SHORTCUTS => ', err);
    });
  };



  const checkExistsShortCut = () => {
    exists('id1').then(function (e) {
      console.log('RN APP SHORTCUTS => ', e);
    }).catch(function (err) {
      console.log('ERRO RN APP SHORTCUTS => ', err);
    });
  };

  const removeAllShortCut = () => {
    removeShortcut('id1');
  };

  const getDrawableImages = async () => {
    try {
      const imageNames = await getDrawableImageNames();
      // const images = imageNames.map(imageName => ({ uri: imageName }));
      // setImages(images);
      console.log('GET DRAWABLE IMAGES IMAGES => ', imageNames);
    } catch (error) {
      console.log('GET DRAWABLE IMAGES ERROR => ', error);
    }
  };
  const ImageList = ({ images }) => {
    return (
      <FlatList
        data={images}
        renderItem={({ item }) => (
          <View>
            <Image
              source={{ uri: item.uri }}
              style={{ width: 100, height: 100 }}
            />
          </View>
        )}
        keyExtractor={item => item.key}
      />
    );
  };

  return (
    <View style={styles.container}>
      <Text>Open up App.js to start working on your app!</Text>
      {/* <View style={styles.actions}>
        <Button style={styles.buttons} title="Call to my love!" onPress={handleCallNumber}></Button>
      </View> */}
      <View style={styles.actions}>
        <Button style={styles.buttons} title="GET IMAGENS" onPress={getDrawableImages}></Button>
      </View>
      <ImageList images={images} />
      {/* <View style={styles.actions}>
        <Button style={styles.buttons} title="Check permission!" onPress={checkPermission}></Button>
      </View>
      <View style={styles.actions}>
        <Button style={styles.buttons} title="Check shortcut!" onPress={checkExistsShortCut}></Button>
      </View>
      <View style={styles.actions}>
        <Button style={styles.buttons} title="Pinned shortcut!" onPress={pinnedShortcut}></Button>
      </View>
      <View style={styles.actions}>
        <Button style={styles.buttons} title="Added detail to shortcut!" onPress={addedShortCut}></Button>
      </View>
      <View style={styles.actions}>
        <Button style={styles.buttons} title="Remove all shortcut" onPress={removeAllShortCut}></Button>
      </View> */}
      <StatusBar style="auto" />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  actions: {
    // flex:1,
    marginVertical: 20,
    flexDirection: 'column',
    justifyContent: 'center',

    // alignItems:'center',
  },
  buttons: {
    padding: 20,
    marginVertical: 10
  }
});

export default App;
