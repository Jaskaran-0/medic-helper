package ca.mohawk.medichelper.api;

// FIX: Removed unused import 'com.google.android.gms.common.api.Api'. It served no purpose
// and could cause confusion by implying a dependency that isn't there.

import java.util.List;

import data_models.AddFamilyMemberDTO;
import data_models.ApiResponse;
import data_models.Appointment;
import data_models.ChangePasswordDTO;
import data_models.FamilyMember;
import data_models.LoginRequest;
import data_models.AuthResponse;
import data_models.Medication;
import data_models.Note;
import data_models.PendingRequest;
import data_models.RegisterRequest;
import data_models.Reminder;
import data_models.SwitchAccountResponse;
import data_models.TokenValidityResponse;
import data_models.UpdateUserDTO;
import data_models.UserDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/tokenValidityCheck")
    Call<TokenValidityResponse> checkTokenValidity(@Body String token);

    @POST("auth/register")
    Call<AuthResponse> registerUser(@Body RegisterRequest user);

    @POST("users/logout")
    Call<String> logout(@Header("Authorization") String token, @Body String fcmToken);

    @GET("users/me")
    Call<UserDTO> getUserData(@Header("Authorization") String token);

    @PUT("users/update")
    Call<Void> updateUserDetails(@Header("Authorization") String token, @Body UpdateUserDTO updateUserDTO);

    @PUT("users/changePassword")
    Call<String> changePassword(@Header("Authorization") String token, @Body ChangePasswordDTO changePasswordDTO);

    @POST("users/update-fcm-token")
    Call<Void> updateFCMToken(@Header("Authorization") String token, @Body String fcmToken);

    @GET("medications")
    Call<List<Medication>> getMedications(@Header("Authorization") String token);

    @POST("medications")
    Call<Medication> addMedication(@Header("Authorization") String token, @Body Medication medication);

    @PUT("medications")
    Call<Medication> updateMedication(@Header("Authorization") String token, @Body Medication medication);

    @DELETE("medications/{medicationId}")
    Call<Void> deleteMedication(@Header("Authorization") String token, @Path("medicationId") int medicationId);

    @POST("reminders")
    Call<Void> addReminder(@Header("Authorization") String token, @Body Reminder reminder);

    @GET("reminders")
    Call<List<Reminder>> getReminders(@Header("Authorization") String token);

    @PUT("reminders")
    Call<Void> updateReminder(@Header("Authorization") String token, @Body Reminder updateReminderDTO);

    @DELETE("reminders/{reminderId}")
    Call<Void> deleteReminder(@Header("Authorization") String token, @Path("reminderId") int reminderId);

    @GET("notes")
    Call<List<Note>> getNotes(@Header("Authorization") String token);

    @POST("notes")
    Call<Note> addNote(@Header("Authorization") String token, @Body Note note);

    @DELETE("notes/{id}")
    Call<Void> deleteNote(@Header("Authorization") String token, @Path("id") int noteId);

    @GET("appointments")
    Call<List<Appointment>> getAppointments(@Header("Authorization") String token);

    @POST("appointments")
    Call<Appointment> addAppointment(@Header("Authorization") String token, @Body Appointment appointment);

    @DELETE("appointments/{id}")
    Call<Void> deleteAppointment(@Header("Authorization") String token, @Path("id") int appointmentId);

    // FAMILY MANAGEMENT
    @POST("family/send-request")
    Call<Void> sendFamilyRequest(@Header("Authorization") String token, @Body AddFamilyMemberDTO request);

    @POST("family/approve-request/{familyMemberId}")
    Call<Void> approveFamilyRequest(@Header("Authorization") String token, @Path("familyMemberId") int familyMemberId);

    @POST("family/reject-request/{familyMemberId}")
    Call<Void> rejectFamilyRequest(@Header("Authorization") String token, @Path("familyMemberId") int familyMemberId);

    @GET("family/pending-requests")
    Call<List<PendingRequest>> getPendingRequests(@Header("Authorization") String token);

    @GET("family/list")
    Call<List<FamilyMember>> getFamilyMembers(@Header("Authorization") String token);

    @DELETE("family/remove/{id}")
    Call<Void> removeFamilyMember(@Header("Authorization") String token, @Path("id") int familyMemberId);

    @GET("family/switch/{id}")
    Call<SwitchAccountResponse> switchToFamilyMember(@Header("Authorization") String token, @Path("id") String familyUserId);
}
