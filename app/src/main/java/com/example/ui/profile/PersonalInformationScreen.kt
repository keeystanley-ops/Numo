package com.example.ui.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground

@Composable
fun PersonalInformationScreen(
  initialName: String,
  initialEmail: String,
  onSaveProfile: (name: String, email: String, phone: String, dob: String) -> Unit = { _, _, _, _ -> },
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var fullName by remember(initialName) { mutableStateOf(initialName.ifBlank { "Ayesha Njeri" }) }
  var email by remember(initialEmail) { mutableStateOf(initialEmail.ifBlank { "ayesha.njeri@email.com" }) }
  var phone by remember { mutableStateOf("+254 745 345 678") }
  var dob by remember { mutableStateOf("12/04/1996") }

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 1)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(6.dp))

      ProfileSubScreenTopBar(
        title = "Personal Information",
        onBack = onBack,
        showBackLabel = false
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Centered Avatar with camera badge
      Box(
        modifier = Modifier.size(106.dp),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(104.dp)
            .shadow(12.dp, CircleShape, spotColor = Color(0x400A6F62))
            .clip(CircleShape)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color(0xFFA6F4EC), Color(0xFF42C5B8))
              )
            )
            .border(2.5.dp, Color.White, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_user_avatar),
            contentDescription = "Profile Avatar",
            modifier = Modifier
              .size(96.dp)
              .clip(CircleShape),
            contentScale = ContentScale.Crop
          )
        }

        // Camera badge on bottom right
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(30.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(NumoDS.Black)
            .border(1.5.dp, Color.White, CircleShape)
            .clickable {
              Toast.makeText(context, "Avatar photo updated", Toast.LENGTH_SHORT).show()
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Change Photo",
            tint = Color.White,
            modifier = Modifier.size(15.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = fullName.ifBlank { "Ayesha Njeri" },
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(18.dp))

      ProfileRecessedInput(
        label = "Full Name",
        value = fullName,
        onValueChange = { fullName = it },
        placeholder = "Enter full name"
      )

      Spacer(modifier = Modifier.height(12.dp))

      ProfileRecessedInput(
        label = "Email",
        value = email,
        onValueChange = { email = it },
        placeholder = "name@email.com",
        keyboardType = KeyboardType.Email
      )

      Spacer(modifier = Modifier.height(12.dp))

      ProfileRecessedInput(
        label = "Phone Number",
        value = phone,
        onValueChange = { phone = it },
        placeholder = "+254 700 000 000",
        keyboardType = KeyboardType.Phone
      )

      Spacer(modifier = Modifier.height(12.dp))

      ProfileRecessedInput(
        label = "Date of Birth",
        value = dob,
        onValueChange = { dob = it },
        placeholder = "DD/MM/YYYY"
      )

      Spacer(modifier = Modifier.height(22.dp))

      // Secondary teal claymorphic pill (matches screenshot's upper teal pill)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
          .shadow(8.dp, RoundedCornerShape(50), spotColor = Color(0x400A6F62))
          .clip(RoundedCornerShape(50))
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xFF4ADAD0), NumoDS.HeroTealMid, NumoDS.HeroTealDark)
            )
          )
          .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(50))
          .clickable {
            Toast.makeText(context, "Profile details verified", Toast.LENGTH_SHORT).show()
          },
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Change Photo",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Primary dark-teal Save Changes button
      NumoPrimaryButton(
        text = "Save Changes",
        onClick = {
          onSaveProfile(fullName.trim(), email.trim(), phone.trim(), dob.trim())
          Toast.makeText(context, "Personal information saved", Toast.LENGTH_SHORT).show()
          onBack()
        },
        modifier = Modifier.fillMaxWidth(),
        height = 50.dp
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
